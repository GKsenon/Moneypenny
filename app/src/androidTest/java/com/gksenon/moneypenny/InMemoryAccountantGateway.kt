package com.gksenon.moneypenny

import com.gksenon.moneypenny.domain.AccountantGateway
import com.gksenon.moneypenny.domain.dto.PlayerDto
import com.gksenon.moneypenny.domain.dto.TransactionDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class InMemoryAccountantGateway : AccountantGateway {

    private var startingMoney: Int = 0
    private val playersFlow = MutableStateFlow(emptyList<PlayerDto>())
    private val transactionsFlow = MutableStateFlow(emptyList<TransactionDto>())

    override suspend fun saveStartingMoney(startingMoney: Int) {
        this.startingMoney = startingMoney
    }

    override suspend fun getStartingMoney(): Int = startingMoney

    override suspend fun savePlayer(player: PlayerDto) {
        playersFlow.value = playersFlow.value.plus(player)
    }

    override suspend fun getPlayer(playerId: UUID): PlayerDto =
        playersFlow.value.first { it.id == playerId }

    override fun getPlayers(): Flow<List<PlayerDto>> = playersFlow

    override fun getTransactions(): Flow<List<TransactionDto>> = transactionsFlow

    override fun getLastTransaction(): Flow<TransactionDto?> =
        transactionsFlow.map { it.lastOrNull() }

    override suspend fun saveTransaction(transaction: TransactionDto) {
        transactionsFlow.value = transactionsFlow.value.plus(transaction)
    }

    override suspend fun deleteTransaction(id: UUID) {
        transactionsFlow.value = transactionsFlow.value.filter { it.id != id }
    }

    override suspend fun clear() {
        startingMoney = 0
        playersFlow.value = emptyList()
        transactionsFlow.value = emptyList()
    }
}