package com.gksenon.moneypenny.di

import com.gksenon.moneypenny.data.NearbyClientMatchMakerGateway
import com.gksenon.moneypenny.data.NearbyHostMatchMakerGateway
import com.gksenon.moneypenny.domain.ClientMatchMaker
import com.gksenon.moneypenny.domain.HostMatchMaker
import com.gksenon.moneypenny.domain.LocalMatchMaker
import com.google.android.gms.nearby.connection.ConnectionsClient
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
    fun provideHostMatchMaker(connectionsClient: ConnectionsClient): HostMatchMaker =
        HostMatchMaker(NearbyHostMatchMakerGateway(connectionsClient))

    @Provides
    @Singleton
    fun provideClientMatchMaker(connectionsClient: ConnectionsClient): ClientMatchMaker =
        ClientMatchMaker(NearbyClientMatchMakerGateway(connectionsClient))
}