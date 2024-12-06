package com.gksenon.moneypenny.di

import com.gksenon.moneypenny.data.InMemoryAccountantGateway
import com.gksenon.moneypenny.data.NearbyClientAccountantGateway
import com.gksenon.moneypenny.data.NearbyHostAccountantGateway
import com.gksenon.moneypenny.domain.Accountant
import com.gksenon.moneypenny.domain.LOCAL_GAME
import com.gksenon.moneypenny.domain.MULTIPLAYER_CLIENT_GAME
import com.gksenon.moneypenny.domain.MULTIPLAYER_HOST_GAME
import com.google.android.gms.nearby.connection.ConnectionsClient
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
    fun provideAccountant(gameRepository: InMemoryAccountantGateway): Accountant =
        Accountant(gameRepository)

    @Singleton
    @Provides
    @Named(MULTIPLAYER_HOST_GAME)
    fun provideHostAccountant(connectionsClient: ConnectionsClient): Accountant {
        val gateway = NearbyHostAccountantGateway(connectionsClient)
        return Accountant(gateway)
    }

    @Singleton
    @Provides
    @Named(MULTIPLAYER_CLIENT_GAME)
    fun provideClientAccountant(connectionsClient: ConnectionsClient): Accountant {
        val gateway = NearbyClientAccountantGateway(connectionsClient)
        return Accountant(gateway)
    }
}