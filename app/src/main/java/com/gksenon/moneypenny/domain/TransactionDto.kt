package com.gksenon.moneypenny.domain

import org.joda.time.Instant
import java.util.UUID

data class TransactionDto(
    val id: UUID,
    val time: Instant,
    val amount: Int,
    val senderId: UUID,
    val recipientId: UUID
)
