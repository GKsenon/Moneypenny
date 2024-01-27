package com.gksenon.moneypenny.domain

import kotlinx.coroutines.flow.Flow
import org.joda.time.Instant
import java.util.UUID

class Accountant(private val gateway: AccountantGateway) {

    suspend fun startGame(startingMoney: Int) {
        if (startingMoney != 0) {
            val transaction = Transaction(
                id = UUID.randomUUID(),
                time = Instant.now(),
                amount = startingMoney
            )
            gateway.writeTransaction(transaction)
        }
    }

    fun getTransactionHistory(): Flow<List<Transaction>> = gateway.readTransactions()

    suspend fun add(amount: Int) {
        if (amount != 0) {
            val transaction = Transaction(
                id = UUID.randomUUID(),
                time = Instant.now(),
                amount = amount
            )
            gateway.writeTransaction(transaction)
        }
    }

    suspend fun subtract(amount: Int) {
        if (amount != 0) {
            val transaction = Transaction(
                id = UUID.randomUUID(),
                time = Instant.now(),
                amount = -amount
            )
            gateway.writeTransaction(transaction)
        }
    }

    suspend fun finishGame() {
        gateway.clear()
    }
}