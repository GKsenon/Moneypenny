package com.gksenon.moneypenny.di

import android.content.Context
import androidx.room.Room
import com.gksenon.moneypenny.data.AccountantDatabase
import com.gksenon.moneypenny.data.RoomAccountantGateway
import com.gksenon.moneypenny.domain.Accountant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AccountantModule {

    @Provides
    fun provideAccountant(@ApplicationContext context: Context): Accountant {
        val db = Room.databaseBuilder(context, AccountantDatabase::class.java, "accountant").build()
        val dao = db.transactionsDao()
        val gateway = RoomAccountantGateway(dao)
        return Accountant(gateway)
    }
}