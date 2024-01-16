package com.gksenon.moneypenny.di

import com.gksenon.moneypenny.data.InMemoryAccountant
import com.gksenon.moneypenny.domain.Accountant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AccountantModule {

    @Provides
    fun provideAccountant(): Accountant = InMemoryAccountant()
}