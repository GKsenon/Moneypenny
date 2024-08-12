package com.gksenon.moneypenny.domain

import com.gksenon.moneypenny.domain.dto.TransactionDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.joda.time.Instant
import java.util.UUID

val BANK_ID = UUID.nameUUIDFromBytes("Bank".toByteArray()).toString()


class Accountant(private val gateway: AccountantGateway) {

    val isGameStarted = gateway.getPlayers().map { it.isNotEmpty() }

    fun getPlayers(): Flow<List<Player>> = gateway.getPlayers()
        .combine(gateway.getTransactions()) { players, transactions ->
            val startingMoney = gateway.getStartingMoney()
            players.map { player ->
                if (player.id == BANK_ID) {
                    Player(id = player.id, name = player.name, balance = Int.MAX_VALUE)
                } else {
                    val income = transactions
                        .filter { transaction -> transaction.recipientId == player.id }
                        .sumOf { it.amount }
                    val outcome = transactions
                        .filter { transaction -> transaction.senderId == player.id }
                        .sumOf { it.amount }
                    Player(
                        id = player.id,
                        name = player.name,
                        balance = startingMoney + income - outcome
                    )
                }
            }
        }

    suspend fun sendMoney(senderId: String, recipientId: String, amount: Int) {
        val transaction = TransactionDto(
            id = UUID.randomUUID().toString(),
            time = Instant.now(),
            amount = amount,
            senderId = senderId,
            recipientId = recipientId
        )
        gateway.saveTransaction(transaction)
    }

    fun getLastTransaction(): Flow<Transaction?> = gateway.getLastTransaction().map { transaction ->
        transaction?.let {
            val sender = gateway.getPlayer(it.senderId)
            val recipient = gateway.getPlayer(it.recipientId)
            Transaction(
                id = it.id,
                time = it.time,
                amount = it.amount,
                sender = Player(id = sender.id, name = sender.name, balance = 0),
                recipient = Player(id = recipient.id, name = recipient.name, balance = 0)
            )
        }
    }

    suspend fun cancelTransaction(transactionId: String) {
        gateway.deleteTransaction(transactionId)
    }

    suspend fun finishGame() {
        gateway.clear()
    }
}

data class Player(val id: String, val name: String, val balance: Int)

data class Transaction(
    val id: String,
    val time: Instant,
    val amount: Int,
    val sender: Player,
    val recipient: Player
)
