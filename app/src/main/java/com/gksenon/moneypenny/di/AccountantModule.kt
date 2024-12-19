package com.gksenon.moneypenny.di

import com.gksenon.moneypenny.data.InMemoryAccountantGateway
import com.gksenon.moneypenny.data.NearbyClientGateway
import com.gksenon.moneypenny.data.NearbyHostGateway
import com.gksenon.moneypenny.domain.Accountant
import com.gksenon.moneypenny.domain.LOCAL_GAME
import com.gksenon.moneypenny.domain.MULTIPLAYER_CLIENT_GAME
import com.gksenon.moneypenny.domain.MULTIPLAYER_HOST_GAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AccountantModule {

    @Singleton
    @Provides
    @Named(LOCAL_GAME)
    fun provideAccountant(gateway: InMemoryAccountantGateway): Accountant = Accountant(gateway)

    @Singleton
    @Provides
    @Named(MULTIPLAYER_HOST_GAME)
    fun provideHostAccountant(gateway: NearbyHostGateway): Accountant = Accountant(gateway)

    @Singleton
    @Provides
    @Named(MULTIPLAYER_CLIENT_GAME)
    fun provideClientAccountant(gateway: NearbyClientGateway): Accountant = Accountant(gateway)
}