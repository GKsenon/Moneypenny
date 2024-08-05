package com.gksenon.moneypenny.data

import com.gksenon.moneypenny.domain.ClientMatchMaker
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class NearbyClientMatchMakerGateway(private val connectionsClient: ConnectionsClient) :
    ClientMatchMaker.Gateway {

    private val status = MutableStateFlow(ClientMatchMaker.ConnectionStatus.IDLE)

    private var clientName: String = ""

    private val discoveryCallback = object : EndpointDiscoveryCallback() {

        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            connectionsClient.requestConnection(clientName, endpointId, connectionsCallback)
            status.update { ClientMatchMaker.ConnectionStatus.CONNECTING }
            println("NearbyClient: endpoint fount: ${info.endpointName}")
        }

        override fun onEndpointLost(endpointId: String) {
            status.update { ClientMatchMaker.ConnectionStatus.DISCOVERY }
            println("NearbyClient: endpoint lost")
        }
    }
    private val connectionsCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
            status.update { ClientMatchMaker.ConnectionStatus.CONNECTING }
            println("NearbyClient: connection initiated: ${info.endpointName}")
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            val status =
                if (resolution.status.isSuccess) ClientMatchMaker.ConnectionStatus.ACCEPTED else ClientMatchMaker.ConnectionStatus.REJECTED
            this@NearbyClientMatchMakerGateway.status.update { status }
            println("NearbyClient: on connection result: ${resolution.status.isSuccess}")
        }

        override fun onDisconnected(endpointId: String) {
            status.update { ClientMatchMaker.ConnectionStatus.REJECTED }
            println("NearbyClient: on connection disconnected")
        }
    }
    private val payloadCallback = object : PayloadCallback() {

        override fun onPayloadReceived(p0: String, p1: Payload) {

        }

        override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {

        }
    }

    override fun startDiscovery(name: String) {
        clientName = name
        val options = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        connectionsClient.startDiscovery("com.gksenon.moneypenny.multiplayer", discoveryCallback, options)
            .addOnSuccessListener {
                println("NearbyClient: discovery started")
            }
            .addOnFailureListener {
                println("NearbyClient: discovery stopped")
            }
        status.update { ClientMatchMaker.ConnectionStatus.DISCOVERY }
    }

    override fun getConnectionStatus() = status

    override fun stopDiscovery() {
        connectionsClient.stopDiscovery()
        status.update { ClientMatchMaker.ConnectionStatus.IDLE }
    }
}
