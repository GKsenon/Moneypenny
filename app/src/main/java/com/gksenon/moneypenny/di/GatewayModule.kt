package com.gksenon.moneypenny.di

import android.content.Context
import com.gksenon.moneypenny.data.InMemoryGateway
import com.gksenon.moneypenny.data.NearbyClientGateway
import com.gksenon.moneypenny.data.NearbyHostGateway
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
class GatewayModule {

    @Provides
    @Singleton
    fun provideConnectionsClient(@ApplicationContext context: Context) =
        Nearby.getConnectionsClient(context)

    @Provides
    @Singleton
    fun provideNearbyClientGateway(connectionsClient: ConnectionsClient) =
        NearbyClientGateway(connectionsClient)

    @Provides
    @Singleton
    fun provideNearbyHostGateway(connectionsClient: ConnectionsClient) =
        NearbyHostGateway(connectionsClient)

    @Provides
    @Singleton
    fun provideInMemoryGateway() = InMemoryGateway()
}