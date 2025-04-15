package com.gksenon.moneypenny.domain

import kotlinx.coroutines.flow.Flow

class ClientMatchMaker(private val gateway: Gateway) {

    val status = gateway.getConnectionStatus()

    suspend fun connectToHost(ip: String, port: Int) {
        gateway.connectToHost(ip, port)
    }

    suspend fun close() {
        gateway.close()
    }

    enum class ConnectionStatus {
        IDLE, CONNECTING, ACCEPTED, REJECTED, STARTED
    }

    interface Gateway {

        fun getConnectionStatus(): Flow<ConnectionStatus>

        suspend fun connectToHost(ip: String, port: Int)

        suspend fun close()
    }
}