package com.gksenon.moneypenny.domain

import kotlinx.coroutines.flow.Flow

interface AccountantGateway {

    suspend fun savePlayer(player: Player)

    fun getPlayers(): Flow<List<Player>>

    fun getTransactions(): Flow<List<Transaction>>

    suspend fun saveTransaction(transaction: Transaction)

    suspend fun clear()
}