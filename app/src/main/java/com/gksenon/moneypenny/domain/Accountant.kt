package com.gksenon.moneypenny.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.joda.time.Instant
import java.util.UUID

class Accountant(private val gateway: AccountantGateway) {

    private val bank = Player(id = UUID.nameUUIDFromBytes("Bank".toByteArray()), name = "Bank")

    val isGameStarted = gateway.getPlayers().map { it.isNotEmpty() }

    suspend fun startGame(startingMoney: Int, players: List<String>) {
        if (startingMoney != 0 && players.isNotEmpty()) {
            gateway.savePlayer(bank)

            players.forEach {
                val player = Player(id = UUID.randomUUID(), name = it)
                gateway.savePlayer(player)

                val transaction = Transaction(
                    id = UUID.randomUUID(),
                    time = Instant.now(),
                    amount = startingMoney,
                    senderId = bank.id,
                    recipientId = player.id
                )
                gateway.saveTransaction(transaction)
            }
        }
    }

    fun getPlayers(): Flow<List<Player>> = gateway.getPlayers()
        .combine(gateway.getTransactions()) { players, transactions ->
            players.map { player ->
                if (player.id == bank.id) {
                    player.copy(balance = Int.MAX_VALUE)
                } else {
                    val income =
                        transactions.filter { transaction -> transaction.recipientId == player.id }
                            .sumOf { it.amount }
                    val outcome =
                        transactions.filter { transaction -> transaction.senderId == player.id }
                            .sumOf { it.amount }
                    player.copy(balance = income - outcome)
                }
            }
        }

    suspend fun sendMoney(senderId: UUID, recipientId: UUID, amount: Int) {
        val transaction = Transaction(
            id = UUID.randomUUID(),
            time = Instant.now(),
            amount = amount,
            senderId = senderId,
            recipientId = recipientId
        )
        gateway.saveTransaction(transaction)
    }

    suspend fun finishGame() {
        gateway.clear()
    }
}

data class Player(val id: UUID, val name: String, val balance: Int = 0)

data class Transaction(
    val id: UUID,
    val time: Instant,
    val amount: Int,
    val senderId: UUID,
    val recipientId: UUID
)