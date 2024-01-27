package com.gksenon.moneypenny.data

import com.gksenon.moneypenny.domain.AccountantGateway
import com.gksenon.moneypenny.domain.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomAccountantGateway(private val dao: TransactionsDao) : AccountantGateway {
    override suspend fun writeTransaction(transaction: Transaction) =
        dao.saveTransaction(TransactionEntity(transaction.id, transaction.time, transaction.amount))


    override fun readTransactions(): Flow<List<Transaction>> =
        dao.getTransactions()
            .map { transactions -> transactions.map { Transaction(it.id, it.time, it.amount) } }

    override suspend fun clear() = dao.clearTransactions()
}