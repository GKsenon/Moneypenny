package com.gksenon.moneypenny.di

import com.gksenon.moneypenny.data.InMemoryGateway
import com.gksenon.moneypenny.data.SocketClientGateway
import com.gksenon.moneypenny.data.SocketHostGateway
import com.gksenon.moneypenny.domain.ClientMatchMaker
import com.gksenon.moneypenny.domain.HostMatchMaker
import com.gksenon.moneypenny.domain.LocalMatchMaker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class MatchMakerModule {

    @Provides
    @Singleton
    fun provideLocalMatchMaker(gateway: InMemoryGateway): LocalMatchMaker = LocalMatchMaker(gateway)

    @Provides
    @Singleton
    fun provideHostMatchMaker(gateway: SocketHostGateway): HostMatchMaker = HostMatchMaker(gateway)

    @Provides
    @Singleton
    fun provideClientMatchMaker(gateway: SocketClientGateway): ClientMatchMaker =
        ClientMatchMaker(gateway)
}