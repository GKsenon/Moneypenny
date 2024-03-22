package com.gksenon.moneypenny.data

import com.gksenon.moneypenny.domain.AccountantGateway
import com.gksenon.moneypenny.domain.Player
import com.gksenon.moneypenny.domain.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RoomAccountantGateway(
    private val playersDao: PlayersDao,
    private val transactionsDao: TransactionsDao
) : AccountantGateway {

    override suspend fun savePlayer(player: Player) = withContext(Dispatchers.IO) {
        playersDao.savePlayer(PlayerEntity(player.id, player.name))
    }

    override fun getPlayers(): Flow<List<Player>> = playersDao.getPlayers()
        .map { it.map { playerEntity -> Player(playerEntity.id, playerEntity.name) } }

    override suspend fun saveTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        transactionsDao.saveTransaction(
            TransactionEntity(
                transaction.id,
                transaction.time,
                transaction.amount,
                transaction.senderId,
                transaction.recipientId
            )
        )
    }


    override fun getTransactions(): Flow<List<Transaction>> = transactionsDao.getTransactions()
        .map { transactions ->
            transactions.map {
                Transaction(
                    it.id,
                    it.time,
                    it.amount,
                    it.senderId,
                    it.recipientId
                )
            }
        }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        playersDao.clear()
        transactionsDao.clear()
    }
}