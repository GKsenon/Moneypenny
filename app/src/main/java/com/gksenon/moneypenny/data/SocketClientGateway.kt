package com.gksenon.moneypenny.data

import com.gksenon.moneypenny.domain.Accountant
import com.gksenon.moneypenny.domain.ClientMatchMaker
import com.gksenon.moneypenny.domain.dto.PlayerDto
import com.gksenon.moneypenny.domain.dto.TransactionDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.Socket

class SocketClientGateway :
    ClientMatchMaker.Gateway, Accountant.Gateway {

    private val connectionsStatus = MutableStateFlow(ClientMatchMaker.ConnectionStatus.IDLE)
    private var socket: Socket? = null

    private var startingMoney = 0
    private val players = MutableStateFlow<List<PlayerDto>>(emptyList())
    private val transactions = MutableStateFlow<List<TransactionDto>>(emptyList())

    override suspend fun connectToHost(ip: String, port: Int, name: String): Unit = withContext(Dispatchers.IO) {
        socket = Socket(ip, port)
        val writer = socket?.getOutputStream()?.bufferedWriter()
        val connectionRequest: Message = Message.RequestConnection(name)
        writer?.append(Json.encodeToString(connectionRequest))
        writer?.newLine()
        writer?.flush()
        //TODO: if I close writer, will the socket be closed too?

        connectionsStatus.value = ClientMatchMaker.ConnectionStatus.CONNECTING

        socket?.getInputStream()?.bufferedReader()?.use { reader ->
            while (isActive) {
                val payload = reader.readLine()
                if (payload != null) {
                    val message = Json.decodeFromString<Message>(payload)
                    when (message) {
                        is Message.Accepted -> {
                            connectionsStatus.update { ClientMatchMaker.ConnectionStatus.ACCEPTED }
                        }

                        is Message.Start -> {}
                        is Message.SaveTransaction -> {}
                        is Message.DeleteTransaction -> {}
                        is Message.Finish -> {}
                        else -> {}
                    }
                }
            }
        }
    }

    override suspend fun close() {
        //TODO: close, when game is not started, keep, if in progress
        withContext(Dispatchers.IO) { socket?.close() }
        connectionsStatus.update { ClientMatchMaker.ConnectionStatus.IDLE }
    }

    override fun getConnectionStatus() = connectionsStatus.asStateFlow()

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
