package com.gksenon.moneypenny.data

import com.gksenon.moneypenny.domain.Accountant
import com.gksenon.moneypenny.domain.BANK_ID
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

const val SERVICE_ID = "com.gksenon.moneypenny.multiplayer"

class NearbyHostGateway(private val connectionsClient: ConnectionsClient) :
    HostMatchMaker.Gateway, Accountant.Gateway {

    private val connectionEvents = MutableSharedFlow<HostMatchMaker.ClientConnectionEvent>()
    private val connectionsLifecycleCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            GlobalScope.launch {
                connectionEvents.emit(
                    HostMatchMaker.ClientConnectionEvent.Initiated(
                        playerId = endpointId,
                        name = info.endpointName
                    )
                )
            }
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            val event = if (resolution.status.isSuccess)
                HostMatchMaker.ClientConnectionEvent.Connected(endpointId)
            else
                HostMatchMaker.ClientConnectionEvent.Disconnected(endpointId)
            GlobalScope.launch {
                connectionEvents.emit(event)
            }
        }

        override fun onDisconnected(endpointId: String) {
            val event = HostMatchMaker.ClientConnectionEvent.Disconnected(endpointId)
            connectionEvents.tryEmit(event)
        }
    }
    private val payloadCallback = object : PayloadCallback() {

        override fun onPayloadReceived(endpointId: String, payload: Payload) {

        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {

        }
    }

    override fun startAdvertising() {
        val options = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        connectionsClient.startAdvertising(
            "Moneypenny Multiplayer",
            SERVICE_ID,
            connectionsLifecycleCallback,
            options
        )
    }

    override fun getClientConnectionEvents() = connectionEvents

    override fun acceptConnection(connectionId: String) {
        connectionsClient.acceptConnection(connectionId, payloadCallback)
    }

    override fun rejectConnection(connectionId: String) {
        connectionsClient.rejectConnection(connectionId)
    }

    override fun stopAdvertising() {
        connectionsClient.stopAdvertising()
    }

    private var startingMoney = 0
    private val players = MutableStateFlow<List<PlayerDto>>(emptyList())
    private val transactions = MutableStateFlow<List<TransactionDto>>(emptyList())

    override fun saveGameParams(startingMoney: Int, players: List<PlayerDto>) {
        this.startingMoney = startingMoney
        this.players.value = players
        for (player in players.filter { it.id != BANK_ID }) {
            val message: Message = Message.Start(
                startingMoney = startingMoney,
                players = players.map { PlayerEntity(it.id, it.name) }
            )
            val payload = Payload.fromBytes(
                Json.encodeToString(message).toByteArray(charset = Charsets.UTF_8)
            )
            connectionsClient.sendPayload(player.id, payload)
        }
    }

    override fun getStartingMoney(): Int = startingMoney

    override fun getPlayer(playerId: String): PlayerDto? = players.value.find { it.id == playerId }

    override fun getPlayers(): Flow<List<PlayerDto>> = players

    override fun getTransactions(): Flow<List<TransactionDto>> = transactions

    override fun getLastTransaction(): Flow<TransactionDto?> = transactions.map { it.lastOrNull() }

    override suspend fun saveTransaction(transaction: TransactionDto) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTransaction(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun clear() {
        TODO("Not yet implemented")
    }
}
