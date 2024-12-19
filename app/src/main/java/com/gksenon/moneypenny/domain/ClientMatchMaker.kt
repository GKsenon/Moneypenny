package com.gksenon.moneypenny.domain

import kotlinx.coroutines.flow.Flow

class ClientMatchMaker(private val gateway: Gateway) {

    val status = gateway.getConnectionStatus()

    fun startDiscovery(name: String) = gateway.startDiscovery(name)

    fun reset() = gateway.stopDiscovery()

    enum class ConnectionStatus {
        IDLE, DISCOVERY, CONNECTING, ACCEPTED, REJECTED, STARTED
    }

    interface Gateway {

        fun getConnectionStatus(): Flow<ConnectionStatus>

        fun startDiscovery(name: String)

        fun stopDiscovery()
    }
}