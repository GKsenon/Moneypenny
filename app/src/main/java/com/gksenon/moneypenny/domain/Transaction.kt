package com.gksenon.moneypenny.domain

import org.joda.time.Instant
import java.util.UUID

data class Transaction(val id: UUID, val time: Instant, val amount: Int)