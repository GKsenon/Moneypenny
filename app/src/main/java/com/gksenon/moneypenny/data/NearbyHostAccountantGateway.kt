package com.gksenon.moneypenny.data

import com.gksenon.moneypenny.domain.Accountant
import com.gksenon.moneypenny.domain.dto.PlayerDto
import com.gksenon.moneypenny.domain.dto.TransactionDto
import com.google.android.gms.nearby.connection.ConnectionsClient
import kotlinx.coroutines.flow.Flow

class NearbyHostAccountantGateway(private val connectionsClient: ConnectionsClient): Accountant.Gateway {

    override fun saveStartingMoney(startingMoney: Int) {
        TODO("Not yet implemented")
    }

    override fun savePlayer(player: PlayerDto) {
        TODO("Not yet implemented")
    }

    override fun getStartingMoney(): Int {
        TODO("Not yet implemented")
    }

    override fun getPlayer(playerId: String): PlayerDto? {
        TODO("Not yet implemented")
    }

    override fun getPlayers(): Flow<List<PlayerDto>> {
        TODO("Not yet implemented")
    }

    override fun getTransactions(): Flow<List<TransactionDto>> {
        TODO("Not yet implemented")
    }

    override fun getLastTransaction(): Flow<TransactionDto?> {
        TODO("Not yet implemented")
    }

    override suspend fun saveTransaction(transaction: TransactionDto) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTransaction(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun clear() {
        TODO("Not yet implemented")
    }
}