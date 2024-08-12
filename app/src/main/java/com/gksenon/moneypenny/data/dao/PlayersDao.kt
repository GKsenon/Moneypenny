package com.gksenon.moneypenny.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gksenon.moneypenny.data.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayersDao {

    @Insert
    suspend fun savePlayer(player: PlayerEntity)

    @Query("SELECT * FROM players")
    fun getPlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE id = :id")
    fun getPlayer(id: String): PlayerEntity

    @Query("DELETE FROM players")
    fun clear()
}