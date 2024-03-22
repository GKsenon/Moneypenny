package com.gksenon.moneypenny.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [PlayerEntity::class, TransactionEntity::class], version = 1)
@TypeConverters(InstantConverter::class)
abstract class AccountantDatabase: RoomDatabase() {

    abstract fun playersDao(): PlayersDao

    abstract fun transactionsDao(): TransactionsDao
}