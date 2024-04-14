package com.gksenon.moneypenny.domain

import kotlinx.coroutines.flow.Flow
import org.joda.time.Instant
import java.util.UUID

interface AccountantGateway {

    suspend fun saveStartingMoney(startingMoney: Int)

    suspend fun getStartingMoney(): Int

    suspend fun savePlayer(player: PlayerDto)

    suspend fun getPlayer(playerId: UUID): PlayerDto

    fun getPlayers(): Flow<List<PlayerDto>>

    fun getTransactions(): Flow<List<TransactionDto>>

    fun getLastTransaction(): Flow<TransactionDto?>

    suspend fun saveTransaction(transaction: TransactionDto)

    suspend fun deleteTransaction(id: UUID)

    suspend fun clear()
}

data class PlayerDto(val id: UUID, val name: String)

data class TransactionDto(
    val id: UUID,
    val time: Instant,
    val amount: Int,
    val senderId: UUID,
    val recipientId: UUID
)