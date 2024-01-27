package com.gksenon.moneypenny.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.Instant
import java.util.UUID

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: UUID,
    val time: Instant,
    val amount: Int
)