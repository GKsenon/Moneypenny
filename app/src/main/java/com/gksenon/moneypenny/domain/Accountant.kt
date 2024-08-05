package com.gksenon.moneypenny.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.joda.time.Instant
import java.util.UUID

class Accountant(private val gateway: AccountantGateway) {

    private val bankId = UUID.nameUUIDFromBytes("Bank".toByteArray())

    val isGameStarted = gateway.getPlayers().map { it.isNotEmpty() }

    fun validateGameParams(
        startingMoney: Int,
        players: List<String>
    ): List<GameParamsValidationError> = buildList {
        if (startingMoney <= 0)
            add(GameParamsValidationError.STARTING_MONEY_IS_INVALID)
        if (players.size !in 2..8)
            add(GameParamsValidationError.PLAYERS_AMOUNT_IS_INVALID)
        if (players.toSet().size != players.size)
            add(GameParamsValidationError.PLAYERS_NOT_UNIQUE)
    }

    suspend fun startGame(startingMoney: Int, players: List<String>) {
        val gameParamsValidationErrors = validateGameParams(startingMoney, players)
        if (gameParamsValidationErrors.isEmpty()) {
            gateway.saveStartingMoney(startingMoney)
            gateway.savePlayer(PlayerDto(id = bankId, name = "Bank"))
            players.forEach {
                val player = PlayerDto(id = UUID.randomUUID(), name = it)
                gateway.savePlayer(player)
            }
        }
    }

    fun getPlayers(): Flow<List<Player>> = gateway.getPlayers()
        .combine(gateway.getTransactions()) { players, transactions ->
            val startingMoney = gateway.getStartingMoney()
            players.map { player ->
                if (player.id == bankId) {
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

    suspend fun sendMoney(senderId: UUID, recipientId: UUID, amount: Int) {
        val transaction = TransactionDto(
            id = UUID.randomUUID(),
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

    suspend fun cancelTransaction(transactionId: UUID) {
        gateway.deleteTransaction(transactionId)
    }

    suspend fun finishGame() {
        gateway.clear()
    }
}

data class Player(val id: UUID, val name: String, val balance: Int)

data class Transaction(
    val id: UUID,
    val time: Instant,
    val amount: Int,
    val sender: Player,
    val recipient: Player
)
