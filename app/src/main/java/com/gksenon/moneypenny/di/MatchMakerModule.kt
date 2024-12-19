package com.gksenon.moneypenny.di

import com.gksenon.moneypenny.data.NearbyClientGateway
import com.gksenon.moneypenny.data.NearbyHostGateway
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
    fun provideLocalMatchMaker(): LocalMatchMaker = LocalMatchMaker()

    @Provides
    @Singleton
    fun provideHostMatchMaker(gateway: NearbyHostGateway): HostMatchMaker = HostMatchMaker(gateway)

    @Provides
    @Singleton
    fun provideClientMatchMaker(gateway: NearbyClientGateway): ClientMatchMaker =
        ClientMatchMaker(gateway)
}