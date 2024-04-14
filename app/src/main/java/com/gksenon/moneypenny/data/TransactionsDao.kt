package com.gksenon.moneypenny.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface TransactionsDao {

    @Insert
    suspend fun saveTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions")
    fun getTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY time DESC LIMIT 1")
    fun getLastTransaction(): Flow<TransactionEntity?>

    @Query("DELETE FROM transactions WHERE id = :id")
    fun deleteTransaction(id: UUID)

    @Query("DELETE FROM transactions")
    suspend fun clear()
}