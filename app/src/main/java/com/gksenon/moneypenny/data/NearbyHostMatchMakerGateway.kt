package com.gksenon.moneypenny.data

import com.gksenon.moneypenny.domain.HostMatchMaker
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val SERVICE_ID = "com.gksenon.moneypenny.multiplayer"

class NearbyHostMatchMakerGateway(private val connectionsClient: ConnectionsClient) :
    HostMatchMaker.Gateway {

    private val connectionEvents = MutableSharedFlow<HostMatchMaker.ClientConnectionEvent>()
    private val connectionsLifecycleCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            GlobalScope.launch {
                connectionEvents.emit(
                    HostMatchMaker.ClientConnectionEvent(
                        id = endpointId,
                        name = info.endpointName
                    )
                )
            }
            println("NearbyHost: on connection initiated: ${info.endpointName}")
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            val status = if (resolution.status.isSuccess)
                HostMatchMaker.ConnectionStatus.CONNECTED
            else
                HostMatchMaker.ConnectionStatus.DISCONNECTED
            val event = HostMatchMaker.ClientConnectionEvent(id = endpointId, status = status)
            GlobalScope.launch {
                connectionEvents.emit(event)
            }
            println("NearbyHost: on connection result: ${resolution.status.isSuccess}")
        }

        override fun onDisconnected(endpointId: String) {
            val event = HostMatchMaker.ClientConnectionEvent(
                id = endpointId,
                status = HostMatchMaker.ConnectionStatus.DISCONNECTED
            )
            connectionEvents.tryEmit(event)
            println("NearbyHost: connection disconnected")
        }
    }
    private val payloadCallback = object : PayloadCallback() {

        override fun onPayloadReceived(endpointId: String, payload: Payload) {

        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {

        }
    }

    override fun startAdvertising() {
        val options = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        connectionsClient.startAdvertising(
            "Moneypenny Multiplayer",
            SERVICE_ID,
            connectionsLifecycleCallback,
            options
        ).addOnSuccessListener {
            println("NearbyHost: advertising started")
        }.addOnFailureListener {
            println("NearbyHost: advertising stopped")
        }
    }

    override fun getClientConnectionEvents() = connectionEvents

    override fun acceptConnection(connectionId: String) {
        connectionsClient.acceptConnection(connectionId, payloadCallback)
    }

    override fun rejectConnection(connectionId: String) {
        connectionsClient.rejectConnection(connectionId)
    }

    override fun stopAdvertising() {
        connectionsClient.stopAdvertising()
    }
}


