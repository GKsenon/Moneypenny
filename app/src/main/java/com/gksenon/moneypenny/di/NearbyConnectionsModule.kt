package com.gksenon.moneypenny.di

import android.content.Context
import com.google.android.gms.nearby.Nearby
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NearbyConnectionsModule {

    @Provides
    @Singleton
    fun provideConnectionsClient(@ApplicationContext context: Context) =
        Nearby.getConnectionsClient(context)
}