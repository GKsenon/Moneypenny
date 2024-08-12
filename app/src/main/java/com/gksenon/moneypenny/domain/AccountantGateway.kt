package com.gksenon.moneypenny.domain

import com.gksenon.moneypenny.domain.dto.PlayerDto
import com.gksenon.moneypenny.domain.dto.TransactionDto
import kotlinx.coroutines.flow.Flow

interface AccountantGateway {

    suspend fun saveStartingMoney(startingMoney: Int)

    suspend fun getStartingMoney(): Int

    suspend fun savePlayer(player: PlayerDto)

    suspend fun getPlayer(playerId: String): PlayerDto

    fun getPlayers(): Flow<List<PlayerDto>>

    fun getTransactions(): Flow<List<TransactionDto>>

    fun getLastTransaction(): Flow<TransactionDto?>

    suspend fun saveTransaction(transaction: TransactionDto)

    suspend fun deleteTransaction(id: String)

    suspend fun clear()
}
