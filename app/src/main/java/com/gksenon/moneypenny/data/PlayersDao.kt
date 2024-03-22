package com.gksenon.moneypenny.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayersDao {

    @Insert
    suspend fun savePlayer(player: PlayerEntity)

    @Query("SELECT * FROM players")
    fun getPlayers(): Flow<List<PlayerEntity>>

    @Query("DELETE FROM players")
    fun clear()
}