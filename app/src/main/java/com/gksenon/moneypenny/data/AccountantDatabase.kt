package com.gksenon.moneypenny.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [TransactionEntity::class], version = 1)
@TypeConverters(InstantConverter::class)
abstract class AccountantDatabase: RoomDatabase() {

    abstract fun transactionsDao(): TransactionsDao
}