package com.gksenon.moneypenny.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gksenon.moneypenny.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionsDao {

    @Insert
    suspend fun saveTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions")
    fun getTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY time DESC LIMIT 1")
    fun getLastTransaction(): Flow<TransactionEntity?>

    @Query("DELETE FROM transactions WHERE id = :id")
    fun deleteTransaction(id: String)

    @Query("DELETE FROM transactions")
    suspend fun clear()
}