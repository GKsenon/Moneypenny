package com.gksenon.moneypenny.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionsDao {

    @Insert
    suspend fun saveTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions")
    fun getTransactions(): Flow<List<TransactionEntity>>

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()
}