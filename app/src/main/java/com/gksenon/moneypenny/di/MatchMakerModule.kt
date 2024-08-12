package com.gksenon.moneypenny.di

import android.content.Context
import com.gksenon.moneypenny.data.InMemoryGameRepository
import com.gksenon.moneypenny.data.NearbyClientMatchMakerGateway
import com.gksenon.moneypenny.data.NearbyHostMatchMakerGateway
import com.gksenon.moneypenny.domain.ClientMatchMaker
import com.gksenon.moneypenny.domain.HostMatchMaker
import com.gksenon.moneypenny.domain.LocalMatchMaker
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class MatchMakerModule {

    @Provides
    @Singleton
    fun provideConnectionsClient(@ApplicationContext context: Context) =
        Nearby.getConnectionsClient(context)

    @Provides
    @Singleton
    fun provideLocalMatchMaker(gameRepository: InMemoryGameRepository): LocalMatchMaker =
        LocalMatchMaker(gameRepository)

    @Provides
    @Singleton
    fun provideHostMatchMaker(connectionsClient: ConnectionsClient): HostMatchMaker =
        HostMatchMaker(NearbyHostMatchMakerGateway(connectionsClient))

    @Provides
    @Singleton
    fun provideClientMatchMaker(connectionsClient: ConnectionsClient): ClientMatchMaker =
        ClientMatchMaker(NearbyClientMatchMakerGateway(connectionsClient))
}