package com.gksenon.moneypenny.domain

import kotlinx.coroutines.flow.Flow

interface AccountantGateway {

    suspend fun writeTransaction(transaction: Transaction)

    fun readTransactions(): Flow<List<Transaction>>

    suspend fun clear()
}