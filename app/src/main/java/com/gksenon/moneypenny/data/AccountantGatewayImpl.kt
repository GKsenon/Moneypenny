package com.gksenon.moneypenny.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gksenon.moneypenny.domain.AccountantGateway
import com.gksenon.moneypenny.domain.PlayerDto
import com.gksenon.moneypenny.domain.TransactionDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withContext
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_session_params")

class AccountantGatewayImpl(
    private val context: Context,
    private val playersDao: PlayersDao,
    private val transactionsDao: TransactionsDao
) : AccountantGateway {

    private val startingMoneyKey = intPreferencesKey("starting_money")

    override suspend fun saveStartingMoney(startingMoney: Int) {
        context.dataStore.edit { gameSessionParams ->
            gameSessionParams[startingMoneyKey] = startingMoney
        }
    }

    override suspend fun getStartingMoney(): Int {
        val preferences = context.dataStore.data.first()
        return preferences[startingMoneyKey] ?: 0
    }

    override suspend fun savePlayer(player: PlayerDto) = withContext(Dispatchers.IO) {
        playersDao.savePlayer(PlayerEntity(player.id, player.name))
    }

    override suspend fun getPlayer(playerId: UUID): PlayerDto {
        val playerEntity = withContext(Dispatchers.IO) { playersDao.getPlayer(playerId) }
        return PlayerDto(playerEntity.id, playerEntity.name)
    }

    override fun getPlayers(): Flow<List<PlayerDto>> = playersDao.getPlayers()
        .map { it.map { playerEntity -> PlayerDto(playerEntity.id, playerEntity.name) } }

    override suspend fun saveTransaction(transaction: TransactionDto) =
        withContext(Dispatchers.IO) {
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


    override fun getTransactions(): Flow<List<TransactionDto>> = transactionsDao.getTransactions()
        .map { transactions ->
            transactions.map {
                TransactionDto(
                    it.id,
                    it.time,
                    it.amount,
                    it.senderId,
                    it.recipientId
                )
            }
        }

    override fun getLastTransaction(): Flow<TransactionDto?> =
        transactionsDao.getLastTransaction().map { transactionEntity ->
            transactionEntity?.let {
                TransactionDto(it.id, it.time, it.amount, it.senderId, it.recipientId)
            }
        }

    override suspend fun deleteTransaction(id: UUID) = withContext(Dispatchers.IO) {
        transactionsDao.deleteTransaction(id)
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        playersDao.clear()
        transactionsDao.clear()
    }
}