package com.gksenon.moneypenny.di

import android.content.Context
import androidx.room.Room
import com.gksenon.moneypenny.data.AccountantDatabase
import com.gksenon.moneypenny.data.AccountantGatewayImpl
import com.gksenon.moneypenny.domain.Accountant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AccountantModule {

    @Singleton
    @Provides
    fun provideAccountant(@ApplicationContext context: Context): Accountant {
        val db = Room.databaseBuilder(context, AccountantDatabase::class.java, "accountant").build()
        val playersDao = db.playersDao()
        val transactionsDao = db.transactionsDao()
        val gateway = AccountantGatewayImpl(context, playersDao, transactionsDao)
        return Accountant(gateway)
    }
}