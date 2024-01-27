package com.gksenon.moneypenny.data

import androidx.room.TypeConverter
import org.joda.time.Instant

class InstantConverter {

    @TypeConverter
    fun toInstant(timestamp: Long) = Instant.ofEpochMilli(timestamp)

    @TypeConverter
    fun toLong(instant: Instant) = instant.millis
}