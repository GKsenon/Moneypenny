package com.gksenon.moneypenny.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gksenon.moneypenny.domain.HostMatchMaker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HostMultiplayerViewModel @Inject constructor(private val matchMaker: HostMatchMaker) :
    ViewModel() {

    private val _state = MutableStateFlow(HostMultiplayerScreenState())
    val state = _state.asStateFlow()

    init {
        matchMaker.connectionEvents.onEach { event ->
            println(event)
            _state.update { previousState ->
                val previousConnections = previousState.connections
                val newConnections =
                    if (event.status == HostMatchMaker.ConnectionStatus.DISCONNECTED) {
                        val connection = previousConnections.find { it.id == event.id }
                        if (connection != null) previousConnections.minus(connection) else previousConnections
                    } else {
                        val status = if (event.status == HostMatchMaker.ConnectionStatus.PENDING)
                            ClientConnectionStatus.PENDING
                        else
                            ClientConnectionStatus.ACCEPTED
                        if (previousConnections.any { it.id == event.id }) {
                            previousConnections.map { if (it.id == event.id) it.copy(status = status) else it }
                        } else {
                            previousConnections.plus(
                                ClientConnection(id = event.id, name = event.name, status = status)
                            )
                        }
                    }
                previousState.copy(connections = newConnections)
            }
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        matchMaker.reset()
    }

    fun onPermissionsGranted() {
        matchMaker.startAdvertising()
    }

    fun onHostNameChanged(value: String) {
        _state.update { previousState -> previousState.copy(hostName = value) }
    }

    fun onStartingMoneyChanged(value: String) {
        _state.update { previousState ->
            val validatedStartingMoney = value.filter { it.isDigit() }.take(9)
            previousState.copy(startingMoney = validatedStartingMoney)
        }
    }

    fun onAcceptPlayerButtonClicked(connectionId: String) {
        matchMaker.acceptConnection(connectionId)
    }

    fun onDenyPlayerButtonClicked(connectionId: String) {
        matchMaker.rejectConnection(connectionId)
    }

    fun onStartButtonClicked() {
        _state.update { previousState -> previousState.copy(showStartGameConfirmationDialog = true) }
    }

    fun onStartGameConfirmationDialogConfirmed() {

    }

    fun onStartGameConfirmationDialogDismissed() {
        _state.update { previousState -> previousState.copy(showStartGameConfirmationDialog = false) }
    }
}

data class HostMultiplayerScreenState(
    val hostName: String = "",
    val startingMoney: String = "",
    val connections: List<ClientConnection> = emptyList(),
    val showStartGameConfirmationDialog: Boolean = false
)

data class ClientConnection(val id: String, val name: String, val status: ClientConnectionStatus)

enum class ClientConnectionStatus { PENDING, ACCEPTED }
