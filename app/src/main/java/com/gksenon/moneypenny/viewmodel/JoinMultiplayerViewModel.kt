package com.gksenon.moneypenny.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gksenon.moneypenny.domain.ClientMatchMaker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class JoinMultiplayerViewModel @Inject constructor(private val matchMaker: ClientMatchMaker) :
    ViewModel() {

    private val _state =
        MutableStateFlow<JoinMultiplayerScreenState>(JoinMultiplayerScreenState.DataInput())
    val state = _state.asStateFlow()

    init {
        matchMaker.status.onEach { status ->
            _state.update {
                when (status) {
                    ClientMatchMaker.ConnectionStatus.IDLE -> JoinMultiplayerScreenState.DataInput()
                    ClientMatchMaker.ConnectionStatus.CONNECTING -> JoinMultiplayerScreenState.ConnectingToHost
                    ClientMatchMaker.ConnectionStatus.ACCEPTED -> JoinMultiplayerScreenState.AcceptedByHost
                    ClientMatchMaker.ConnectionStatus.REJECTED -> JoinMultiplayerScreenState.RejectedByHost
                    ClientMatchMaker.ConnectionStatus.STARTED -> JoinMultiplayerScreenState.GameStarted
                }
            }
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        viewModelScope.launch { matchMaker.close() }
    }

    fun onNameChanged(value: String) = _state.update { previousState ->
        if (previousState is JoinMultiplayerScreenState.DataInput)
            previousState.copy(
                name = value,
                isConfirmButtonEnabled = isNameValid(value) && isHostAddressValid(previousState.hostAddress)
            )
        else
            JoinMultiplayerScreenState.DataInput(name = value)
    }

    fun onHostAddressChanged(value: String) {
        _state.update { previousState ->
            if(previousState is JoinMultiplayerScreenState.DataInput)
                previousState.copy(
                    hostAddress = value,
                    isConfirmButtonEnabled = isNameValid(previousState.name) && isHostAddressValid(value)
                )
            else
                JoinMultiplayerScreenState.DataInput(hostAddress = value)
        }
    }

    fun onConfirmButtonClicked() {
        val state = _state.value as JoinMultiplayerScreenState.DataInput
        val name = state.name
        val ip = state.hostAddress.split(":").first()
        val port = state.hostAddress.split(":").last().toInt()
        viewModelScope.launch { matchMaker.connectToHost(ip, port, name) }
    }

    fun onTryAgainButtonClicked() {
        _state.update { JoinMultiplayerScreenState.DataInput() }
    }

    private fun isNameValid(name: String) = name.isNotEmpty()

    private fun isHostAddressValid(hostAddress: String): Boolean {
        val addressParts = hostAddress.split(":")
        if(addressParts.size != 2)
            return false

        val ipBytes = addressParts.first().split(".")
        if(ipBytes.size != 4)
            return false

        return ipBytes.all { it.toIntOrNull() in 0 .. 256 } && addressParts.last().toIntOrNull() in 0 .. 65535
    }
}

sealed class JoinMultiplayerScreenState {

    data class DataInput(
        val name: String = "",
        val hostAddress: String = "",
        val isConfirmButtonEnabled: Boolean = false
    ) : JoinMultiplayerScreenState()

    data object DiscoveryStarted : JoinMultiplayerScreenState()

    data object ConnectingToHost : JoinMultiplayerScreenState()

    data object AcceptedByHost : JoinMultiplayerScreenState()

    data object RejectedByHost : JoinMultiplayerScreenState()

    data object GameStarted : JoinMultiplayerScreenState()
}