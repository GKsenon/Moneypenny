package com.gksenon.moneypenny.data

import com.gksenon.moneypenny.domain.Accountant
import com.gksenon.moneypenny.domain.ClientMatchMaker
import com.gksenon.moneypenny.domain.dto.PlayerDto
import com.gksenon.moneypenny.domain.dto.TransactionDto
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.joda.time.Instant
import java.util.UUID

class NearbyClientGateway(private val connectionsClient: ConnectionsClient) :
    ClientMatchMaker.Gateway, Accountant.Gateway {

    private val status = MutableStateFlow(ClientMatchMaker.ConnectionStatus.IDLE)

    private var clientName: String = ""
    private var clientId: UUID? = null
    private var hostId: String = ""

    private var startingMoney = 0
    private val players = MutableStateFlow<List<PlayerDto>>(emptyList())
    private val transactions = MutableStateFlow<List<TransactionDto>>(emptyList())

    private val discoveryCallback = object : EndpointDiscoveryCallback() {

        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            connectionsClient.requestConnection(clientName, endpointId, connectionsCallback)
            status.update { ClientMatchMaker.ConnectionStatus.CONNECTING }
        }

        override fun onEndpointLost(endpointId: String) {
            status.update { ClientMatchMaker.ConnectionStatus.DISCOVERY }
        }
    }
    private val connectionsCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
            status.update { ClientMatchMaker.ConnectionStatus.CONNECTING }
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            val status =
                if (resolution.status.isSuccess) ClientMatchMaker.ConnectionStatus.ACCEPTED else ClientMatchMaker.ConnectionStatus.REJECTED
            this@NearbyClientGateway.status.update { status }
        }

        override fun onDisconnected(endpointId: String) {
            status.update { ClientMatchMaker.ConnectionStatus.REJECTED }
        }
    }
    private val payloadCallback = object : PayloadCallback() {

        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val message = Json.decodeFromString<Message>(
                payload.asBytes()?.toString(charset = Charsets.UTF_8) ?: ""
            )
            when(message) {
                is Message.Accepted -> {
                    clientId = UUID.fromString(message.id)
                }
                is Message.Start -> {
                    hostId = endpointId
                    startingMoney = message.startingMoney
                    players.value = message.players.map { PlayerDto(id = it.id, name = it.name) }
                    status.update { ClientMatchMaker.ConnectionStatus.STARTED }
                }
                is Message.SaveTransaction -> {
                    val transactionDto = TransactionDto(
                        id = message.id,
                        time = Instant.ofEpochMilli(message.time),
                        senderId = message.senderId.ifBlank { endpointId },
                        recipientId = message.recipientId.ifBlank { endpointId },
                        amount = message.amount
                    )
                    transactions.value = transactions.value.plus(transactionDto)
                }
                is Message.DeleteTransaction -> {}
                is Message.Finish -> {}
            }
        }

        override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {

        }
    }

    override fun startDiscovery(name: String) {
        clientName = name
        val options = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        connectionsClient.startDiscovery(
            "com.gksenon.moneypenny.multiplayer",
            discoveryCallback,
            options
        )
        status.update { ClientMatchMaker.ConnectionStatus.DISCOVERY }
    }

    override fun getConnectionStatus() = status

    override fun stopDiscovery() {
        connectionsClient.stopDiscovery()
        status.update { ClientMatchMaker.ConnectionStatus.IDLE }
    }

    override fun getStartingMoney(): Int = startingMoney

    override fun getPlayer(playerId: String): PlayerDto? = players.value.find { it.id == playerId }

    override fun getPlayers(): Flow<List<PlayerDto>> = players

    override fun getTransactions(): Flow<List<TransactionDto>> = transactions

    override fun getLastTransaction(): Flow<TransactionDto?> = transactions.map { it.lastOrNull() }

    override suspend fun saveTransaction(transaction: TransactionDto) {
        transactions.value = transactions.value.plus(transaction)
        val saveTransactionMessage: Message = Message.SaveTransaction(
            id = transaction.id,
            time = transaction.time.millis,
            senderId = transaction.senderId,
            recipientId = transaction.recipientId,
            amount = transaction.amount
        )
        val payload = Payload.fromBytes(Json.encodeToString(saveTransactionMessage).toByteArray(charset = Charsets.UTF_8))
        connectionsClient.sendPayload(hostId, payload)
    }

    override suspend fun deleteTransaction(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun clear() {
        TODO("Not yet implemented")
    }
}
