package com.gksenon.moneypenny.domain

class LocalMatchMaker {

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
}
