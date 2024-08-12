package com.gksenon.moneypenny.domain.dto

import org.joda.time.Instant

data class TransactionDto(
    val id: String,
    val time: Instant,
    val amount: Int,
    val senderId: String,
    val recipientId: String
)
