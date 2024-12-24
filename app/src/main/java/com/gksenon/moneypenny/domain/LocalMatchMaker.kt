package com.gksenon.moneypenny.domain

import com.gksenon.moneypenny.domain.dto.PlayerDto
import java.util.UUID

class LocalMatchMaker(private val gateway: Gateway) {

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

    fun startGame(startingMoney: Int, playerNames: List<String>) {
        val bank = PlayerDto(id = BANK_ID, name = "Bank")
        val players = playerNames.map { name ->
            PlayerDto(id = UUID.randomUUID().toString(), name = name)
        }.plus(bank)
        gateway.saveGameParams(startingMoney, players)
    }

    interface Gateway {

        fun saveGameParams(startingMoney: Int, players: List<PlayerDto>)
    }
}
