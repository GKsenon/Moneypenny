package com.gksenon.moneypenny.data

import com.gksenon.moneypenny.domain.Accountant
import com.gksenon.moneypenny.domain.HostMatchMaker
import com.gksenon.moneypenny.domain.dto.PlayerDto
import com.gksenon.moneypenny.domain.dto.TransactionDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.UUID
import kotlin.random.Random
import kotlin.random.nextInt

class SocketHostGateway :
    HostMatchMaker.Gateway, Accountant.Gateway {

    private val clientSockets = mutableMapOf<UUID, Socket>()
    private var serverSocket: ServerSocket? = null

    private val hostAddress = MutableStateFlow("")
    private val clientConnectionEvents = MutableSharedFlow<HostMatchMaker.ClientConnectionEvent>(
        replay = 16,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var startingMoney = 0
    private val players = MutableStateFlow<List<PlayerDto>>(emptyList())
    private val transactions = MutableStateFlow<List<TransactionDto>>(emptyList())

    override suspend fun startAdvertising() = withContext(Dispatchers.IO) {
        val ip = NetworkInterface.getNetworkInterfaces().toList()
            .flatMap { it.inetAddresses.toList() }
            .find { it.isSiteLocalAddress }
        val port = Random.nextInt(1025 until Short.MAX_VALUE)
        serverSocket = ServerSocket(port)
        hostAddress.update { "$ip:$port" }

        while (isActive) {
            val clientSocket = serverSocket?.accept()
            if (clientSocket != null) {
                val clientId = UUID.randomUUID()
                clientSockets[clientId] = clientSocket
                val connectionEvent =
                    HostMatchMaker.ClientConnectionEvent.Initiated(clientId, "")
                clientConnectionEvents.tryEmit(connectionEvent)
            }
        }
    }

    override fun getClientConnectionEvents() = clientConnectionEvents.asSharedFlow()

    override fun getHostAddress() = hostAddress.asStateFlow()

    override suspend fun acceptConnection(playerId: UUID) = withContext(Dispatchers.IO) {
        val clientSocket = clientSockets[playerId]
        if (clientSocket != null) {
            val message: Message = Message.Accepted(playerId.toString())
            val payload = Json.encodeToString(message)
            val writer = clientSocket.getOutputStream().bufferedWriter()
            writer.append(payload)
            writer.newLine()
            writer.flush()

            clientConnectionEvents.tryEmit(HostMatchMaker.ClientConnectionEvent.Connected(playerId))
        }
    }

    override suspend fun rejectConnection(playerId: UUID) = withContext(Dispatchers.IO) {
        val clientSocket = clientSockets[playerId]
        if (clientSocket != null) {
            clientSocket.close()
            clientSockets.remove(playerId)
            clientConnectionEvents.tryEmit(
                HostMatchMaker.ClientConnectionEvent.Disconnected(
                    playerId
                )
            )
        }
    }

    override suspend fun stopAdvertising(): Unit = withContext(Dispatchers.IO) {
            clientSockets.forEach { (_, socket) -> socket.close() }
            clientSockets.clear()
            serverSocket?.close()
        }

    override fun sendStartingMessage(startingMoney: Int, players: List<PlayerDto>) {
        TODO("Not yet implemented")
    }

    override fun getStartingMoney(): Int = startingMoney

    override fun getPlayer(playerId: String): PlayerDto? = players.value.find { it.id == playerId }

    override fun getPlayers(): Flow<List<PlayerDto>> = players

    override fun getTransactions(): Flow<List<TransactionDto>> = transactions

    override fun getLastTransaction(): Flow<TransactionDto?> = transactions.map { it.lastOrNull() }

    override suspend fun saveTransaction(transaction: TransactionDto) {
        transactions.value = transactions.value.plus(transaction)
    }

    override suspend fun deleteTransaction(id: String) {
        transactions.value = transactions.value.filter { it.id != id }
    }

    override suspend fun clear() {
        startingMoney = 0
        players.value = emptyList()
        transactions.value = emptyList()
    }
}
