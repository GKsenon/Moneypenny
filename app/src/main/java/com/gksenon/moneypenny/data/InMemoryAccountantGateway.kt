package com.gksenon.moneypenny.data

import com.gksenon.moneypenny.domain.Accountant
import com.gksenon.moneypenny.domain.dto.PlayerDto
import com.gksenon.moneypenny.domain.dto.TransactionDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryAccountantGateway : Accountant.Gateway {

    private var startingMoney = 0
    private val players = MutableStateFlow<List<PlayerDto>>(emptyList())
    private val transactions = MutableStateFlow<List<TransactionDto>>(emptyList())

    override fun saveGameParams(startingMoney: Int, players: List<PlayerDto>) {
        this.startingMoney = startingMoney
        this.players.value = players
    }

    override fun getStartingMoney(): Int = startingMoney

    override fun getPlayer(playerId: String): PlayerDto? = players.value.find { it.id == playerId }

    override fun getPlayers(): Flow<List<PlayerDto>> = players

    override fun getTransactions(): Flow<List<TransactionDto>> = transactions

    override fun getLastTransaction(): Flow<TransactionDto?> = transactions.map { it.lastOrNull() }

    override suspend fun saveTransaction(transaction: TransactionDto) {
        transactions.value = transactions.value.plus(transaction)
    }

    override suspend fun deleteTransaction(id: String) {
        transactions.value = transactions.value.filter { it.id != id }
    }

    override suspend fun clear() {
        startingMoney = 0
        players.value = emptyList()
        transactions.value = emptyList()
    }
}