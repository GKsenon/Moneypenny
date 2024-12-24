package com.gksenon.moneypenny.data

import com.gksenon.moneypenny.domain.Accountant
import com.gksenon.moneypenny.domain.BANK_ID
import com.gksenon.moneypenny.domain.HOST_ID
import com.gksenon.moneypenny.domain.HostMatchMaker
import com.gksenon.moneypenny.domain.dto.PlayerDto
import com.gksenon.moneypenny.domain.dto.TransactionDto
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.joda.time.Instant
import java.util.UUID

class NearbyHostGateway(private val connectionsClient: ConnectionsClient) :
    HostMatchMaker.Gateway, Accountant.Gateway {

    private val endpointIdToPlayerIdMap = mutableMapOf<String, UUID>()
    private val playerIdToEndpointIdMap = mutableMapOf<UUID, String>()

    private val connectionEvents = MutableSharedFlow<HostMatchMaker.ClientConnectionEvent>(
        replay = 16,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var startingMoney = 0
    private val players = MutableStateFlow<List<PlayerDto>>(emptyList())
    private val transactions = MutableStateFlow<List<TransactionDto>>(emptyList())

    private val connectionsLifecycleCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            val playerId = UUID.randomUUID()
            endpointIdToPlayerIdMap[endpointId] = playerId
            playerIdToEndpointIdMap[playerId] = endpointId
            connectionEvents.tryEmit(
                HostMatchMaker.ClientConnectionEvent.Initiated(
                    id = playerId,
                    name = info.endpointName
                )
            )
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            endpointIdToPlayerIdMap[endpointId]?.let {
                if (resolution.status.isSuccess) {
                    connectionEvents.tryEmit(HostMatchMaker.ClientConnectionEvent.Connected(it))
                    val message: Message = Message.Accepted(it.toString())
                    val payload =
                        Payload.fromBytes(Json.encodeToString(message).toByteArray(Charsets.UTF_8))
                    connectionsClient.sendPayload(endpointId, payload)
                } else {
                    connectionEvents.tryEmit(HostMatchMaker.ClientConnectionEvent.Disconnected(it))
                    endpointIdToPlayerIdMap.remove(endpointId)
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            endpointIdToPlayerIdMap[endpointId]?.let {
                val event = HostMatchMaker.ClientConnectionEvent.Disconnected(it)
                connectionEvents.tryEmit(event)
            }
            endpointIdToPlayerIdMap.remove(endpointId)
        }
    }
    private val payloadCallback = object : PayloadCallback() {

        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val message = Json.decodeFromString<Message>(
                payload.asBytes()?.toString(charset = Charsets.UTF_8) ?: ""
            )
            when (message) {
                is Message.Accepted -> {}
                is Message.Start -> {}
                is Message.SaveTransaction -> {
                    players.value.filter { it.id.isNotBlank() && it.id != BANK_ID && it.id != endpointId }
                        .forEach { player ->
                            connectionsClient.sendPayload(player.id, payload)
                        }
                    val transaction = TransactionDto(
                        id = message.id,
                        time = Instant.ofEpochMilli(message.time),
                        senderId = message.senderId,
                        recipientId = message.recipientId,
                        amount = message.amount
                    )
                    transactions.value = transactions.value.plus(transaction)
                }

                is Message.DeleteTransaction -> {}
                is Message.Finish -> {}
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {

        }
    }

    override fun startAdvertising() {
        val options = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        connectionsClient.startAdvertising(
            "Moneypenny Multiplayer",
            "com.gksenon.moneypenny.multiplayer",
            connectionsLifecycleCallback,
            options
        )
    }

    override fun getClientConnectionEvents() = connectionEvents

    override fun acceptConnection(playerId: UUID) {
        playerIdToEndpointIdMap[playerId]?.let { endpointId ->
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }
    }

    override fun rejectConnection(playerId: UUID) {
        playerIdToEndpointIdMap[playerId]?.let { endpointId ->
            connectionsClient.rejectConnection(endpointId)
        }
    }

    override fun stopAdvertising() {
        connectionsClient.stopAdvertising()
    }

    override fun sendStartingMessage(startingMoney: Int, players: List<PlayerDto>) {
        this.startingMoney = startingMoney
        this.players.value = players
        for (player in players) {
            endpointIdToPlayerIdMap.filterValues { it.toString() == player.id }
                .keys
                .firstOrNull()?.let { endpointId ->
                    val message: Message = Message.Start(
                        startingMoney = startingMoney,
                        players = players.map { player -> PlayerEntity(player.id, player.name) }
                    )
                    val payload = Payload.fromBytes(
                        Json.encodeToString(message).toByteArray(charset = Charsets.UTF_8)
                    )
                    connectionsClient.sendPayload(endpointId, payload)
                }
        }
    }

    override fun getStartingMoney(): Int = startingMoney

    override fun getPlayer(playerId: String): PlayerDto? = players.value.find { it.id == playerId }

    override fun getPlayers(): Flow<List<PlayerDto>> = players

    override fun getTransactions(): Flow<List<TransactionDto>> = transactions

    override fun getLastTransaction(): Flow<TransactionDto?> = transactions.map { it.lastOrNull() }

    override suspend fun saveTransaction(transaction: TransactionDto) {
        transactions.value = transactions.value.plus(transaction)
        players.value.filter { it.id != HOST_ID && it.id != BANK_ID }.forEach { player ->
            val saveTransactionMessage: Message = Message.SaveTransaction(
                id = transaction.id,
                time = transaction.time.millis,
                senderId = transaction.senderId,
                recipientId = transaction.recipientId,
                amount = transaction.amount
            )
            val payload = Payload.fromBytes(
                Json.encodeToString(saveTransactionMessage).toByteArray(charset = Charsets.UTF_8)
            )
            playerIdToEndpointIdMap[UUID.fromString(player.id)]?.let { endpointId ->
                connectionsClient.sendPayload(endpointId, payload)
            }
        }
    }

    override suspend fun deleteTransaction(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun clear() {
        TODO("Not yet implemented")
    }
}
