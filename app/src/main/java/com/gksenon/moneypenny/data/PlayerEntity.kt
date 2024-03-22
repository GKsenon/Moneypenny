package com.gksenon.moneypenny.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey
    val id: UUID,
    val name: String
)