package com.gksenon.moneypenny.domain

import kotlinx.coroutines.flow.Flow

class HostMatchMaker(private val gateway: Gateway) {

    val connectionEvents = gateway.getClientConnectionEvents()

    fun startAdvertising() = gateway.startAdvertising()

    fun acceptConnection(connectionId: String) = gateway.acceptConnection(connectionId)

    fun rejectConnection(connectionId: String) = gateway.rejectConnection(connectionId)

    fun validateGameParams(
        startingMoney: Int,
        players: List<String>
    ): List<GameParamsValidationError> = buildList {
        if (startingMoney <= 0)
            add(GameParamsValidationError.STARTING_MONEY_IS_INVALID)
        if (players.size !in 2..8)
            add(GameParamsValidationError.PLAYERS_AMOUNT_IS_INVALID)
        if (players.toSet().size != players.size)
            add(GameParamsValidationError.PLAYERS_NOT_UNIQUE)
    }

    fun startGame() {

    }

    fun reset() {
        gateway.stopAdvertising()
    }

    data class ClientConnectionEvent(
        val id: String,
        val name: String = "",
        val status: ConnectionStatus = ConnectionStatus.PENDING
    )

    enum class ConnectionStatus { PENDING, CONNECTED, DISCONNECTED }

    interface Gateway {

        fun startAdvertising()

        fun getClientConnectionEvents(): Flow<ClientConnectionEvent>

        fun acceptConnection(connectionId: String)

        fun rejectConnection(connectionId: String)

        fun stopAdvertising()
    }
}