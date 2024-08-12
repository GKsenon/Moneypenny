package com.gksenon.moneypenny.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey
    val id: String,
    val name: String
)