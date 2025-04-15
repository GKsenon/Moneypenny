package com.gksenon.moneypenny.domain

import kotlinx.coroutines.flow.Flow

class ClientMatchMaker(private val gateway: Gateway) {

    val status = gateway.getConnectionStatus()

    suspend fun connectToHost(ip: String, port: Int, name: String) {
        gateway.connectToHost(ip, port, name)
    }

    suspend fun close() {
        gateway.close()
    }

    enum class ConnectionStatus {
        IDLE, CONNECTING, ACCEPTED, REJECTED, STARTED
    }

    interface Gateway {

        fun getConnectionStatus(): Flow<ConnectionStatus>

        suspend fun connectToHost(ip: String, port: Int, name: String)

        suspend fun close()
    }
}