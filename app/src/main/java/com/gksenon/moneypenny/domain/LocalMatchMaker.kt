package com.gksenon.moneypenny.domain

import com.gksenon.moneypenny.domain.dto.PlayerDto
import java.util.UUID

class LocalMatchMaker(private val gateway: Gateway) {

    private val bankId = UUID.nameUUIDFromBytes("Bank".toByteArray()).toString()

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

    fun startGame(startingMoney: Int, players: List<String>) {
        val gameParamsValidationErrors = validateGameParams(startingMoney, players)
        if (gameParamsValidationErrors.isEmpty()) {
            gateway.saveStartingMoney(startingMoney)
            gateway.savePlayer(PlayerDto(id = bankId, name = "Bank"))
            players.forEach {
                val player = PlayerDto(id = UUID.randomUUID().toString(), name = it)
                gateway.savePlayer(player)
            }
        }
    }

    interface Gateway {

        fun saveStartingMoney(startingMoney: Int)

        fun savePlayer(player: PlayerDto)
    }
}
