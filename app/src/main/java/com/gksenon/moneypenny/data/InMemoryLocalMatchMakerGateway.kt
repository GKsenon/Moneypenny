package com.gksenon.moneypenny.data

import com.gksenon.moneypenny.domain.LocalMatchMaker
import com.gksenon.moneypenny.domain.PlayerDto

class InMemoryLocalMatchMakerGateway: LocalMatchMaker.Gateway {

    private var startingMoney = 0
    private val players = mutableListOf<PlayerDto>()

    override fun saveStartingMoney(startingMoney: Int) {
        this.startingMoney = startingMoney
    }

    override fun savePlayer(player: PlayerDto) {
        players.add(player)
    }
}