package com.gksenon.moneypenny.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.Instant

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val time: Instant,
    val amount: Int,
    val senderId: String,
    val recipientId: String
)