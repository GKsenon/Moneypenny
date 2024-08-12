package com.gksenon.moneypenny.di

import com.gksenon.moneypenny.data.InMemoryGameRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideGameRepository() = InMemoryGameRepository()
}