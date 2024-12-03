package com.gksenon.moneypenny.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.scan

class HostMatchMaker(private val gateway: Gateway) {

    val players = gateway.getClientConnectionEvents().scan(emptyList<Player>()) { acc, event ->
        when (event) {
            is ClientConnectionEvent.Initiated -> {
                acc.plus(Player(event.playerId, event.name, PlayerStatus.PENDING))
            }

            is ClientConnectionEvent.Connected -> {
                acc.map { if (it.id == event.playerId) it.copy(status = PlayerStatus.ACCEPTED) else it }
            }

            is ClientConnectionEvent.Disconnected -> {
                val disconnectedPlayer = acc.find { it.id == event.playerId }
                if (disconnectedPlayer != null) acc.minus(disconnectedPlayer) else acc
            }
        }
    }

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

    data class Player(
        val id: String,
        val name: String,
        val status: PlayerStatus
    )

    enum class PlayerStatus { PENDING, ACCEPTED }

    sealed class ClientConnectionEvent {

        data class Initiated(val playerId: String, val name: String) : ClientConnectionEvent()

        data class Connected(val playerId: String) : ClientConnectionEvent()

        data class Disconnected(val playerId: String) : ClientConnectionEvent()
    }

    interface Gateway {

        fun startAdvertising()

        fun getClientConnectionEvents(): Flow<ClientConnectionEvent>

        fun acceptConnection(connectionId: String)

        fun rejectConnection(connectionId: String)

        fun stopAdvertising()
    }
}