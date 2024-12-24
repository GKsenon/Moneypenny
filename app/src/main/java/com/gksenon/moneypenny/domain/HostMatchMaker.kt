package com.gksenon.moneypenny.domain

import com.gksenon.moneypenny.domain.dto.PlayerDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.scan
import java.util.UUID

class HostMatchMaker(private val gateway: Gateway) {

    val players = gateway.getClientConnectionEvents().scan(emptyList<Player>()) { acc, event ->
        when (event) {
            is ClientConnectionEvent.Initiated -> {
                acc.plus(Player(event.id, event.name, PlayerStatus.PENDING))
            }

            is ClientConnectionEvent.Connected -> {
                acc.map { if (it.id == event.id) it.copy(status = PlayerStatus.ACCEPTED) else it }
            }

            is ClientConnectionEvent.Disconnected -> {
                val disconnectedPlayer = acc.find { it.id == event.id }
                if (disconnectedPlayer != null) acc.minus(disconnectedPlayer) else acc
            }
        }
    }

    fun startAdvertising() = gateway.startAdvertising()

    fun acceptConnection(connectionId: UUID) = gateway.acceptConnection(connectionId)

    fun rejectConnection(connectionId: UUID) = gateway.rejectConnection(connectionId)

    fun startGame(hostName: String, startingMoney: Int, players: List<Player>) {
        gateway.stopAdvertising()
        val bank = PlayerDto(id = BANK_ID, name = "Bank")
        val host = PlayerDto(id = HOST_ID, name = hostName)
        gateway.sendStartingMessage(
            startingMoney = startingMoney,
            players = players.map { PlayerDto(it.id.toString(), it.name) } + host + bank
        )
    }

    fun reset() {
        gateway.stopAdvertising()
    }

    data class Player(
        val id: UUID,
        val name: String,
        val status: PlayerStatus
    )

    enum class PlayerStatus { PENDING, ACCEPTED }

    sealed class ClientConnectionEvent {

        data class Initiated(val id: UUID, val name: String) : ClientConnectionEvent()

        data class Connected(val id: UUID) : ClientConnectionEvent()

        data class Disconnected(val id: UUID) : ClientConnectionEvent()
    }

    interface Gateway {

        fun startAdvertising()

        fun getClientConnectionEvents(): Flow<ClientConnectionEvent>

        fun acceptConnection(playerId: UUID)

        fun rejectConnection(playerId: UUID)

        fun stopAdvertising()

        fun sendStartingMessage(startingMoney: Int, players: List<PlayerDto>)
    }
}