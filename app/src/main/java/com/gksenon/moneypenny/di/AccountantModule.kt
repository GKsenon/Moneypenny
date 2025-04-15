package com.gksenon.moneypenny.di

import com.gksenon.moneypenny.data.InMemoryGateway
import com.gksenon.moneypenny.data.SocketClientGateway
import com.gksenon.moneypenny.data.SocketHostGateway
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
    fun provideAccountant(gateway: InMemoryGateway): Accountant = Accountant(gateway)

    @Singleton
    @Provides
    @Named(MULTIPLAYER_HOST_GAME)
    fun provideHostAccountant(gateway: SocketHostGateway): Accountant = Accountant(gateway)

    @Singleton
    @Provides
    @Named(MULTIPLAYER_CLIENT_GAME)
    fun provideClientAccountant(gateway: SocketClientGateway): Accountant = Accountant(gateway)
}