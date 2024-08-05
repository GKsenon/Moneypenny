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
import javax.inject.Inject

@HiltViewModel
class JoinMultiplayerViewModel @Inject constructor(private val matchMaker: ClientMatchMaker) :
    ViewModel() {

    private val _state =
        MutableStateFlow<JoinMultiplayerScreenState>(JoinMultiplayerScreenState.PlayerNameRequested())
    val state = _state.asStateFlow()

    init {
        matchMaker.status.onEach { status ->
            _state.update {
                when (status) {
                    ClientMatchMaker.ConnectionStatus.IDLE -> JoinMultiplayerScreenState.PlayerNameRequested()
                    ClientMatchMaker.ConnectionStatus.DISCOVERY -> JoinMultiplayerScreenState.DiscoveryStarted
                    ClientMatchMaker.ConnectionStatus.CONNECTING -> JoinMultiplayerScreenState.ConnectingToHost
                    ClientMatchMaker.ConnectionStatus.ACCEPTED -> JoinMultiplayerScreenState.AcceptedByHost
                    ClientMatchMaker.ConnectionStatus.REJECTED -> JoinMultiplayerScreenState.RejectedByHost
                }
            }
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        matchMaker.reset()
    }

    fun onNameChanged(value: String) =
        _state.update {
            JoinMultiplayerScreenState.PlayerNameRequested(
                name = value,
                isConfirmButtonEnabled = value.isNotEmpty()
            )
        }

    fun onNameConfirmed() {
        val name = (_state.value as JoinMultiplayerScreenState.PlayerNameRequested).name
        matchMaker.startDiscovery(name)
    }

    fun onTryAgainButtonClicked() {
        val name = (_state.value as JoinMultiplayerScreenState.PlayerNameRequested).name
        matchMaker.startDiscovery(name)
    }
}

sealed class JoinMultiplayerScreenState {

    data class PlayerNameRequested(
        val name: String = "",
        val isConfirmButtonEnabled: Boolean = false
    ) : JoinMultiplayerScreenState()

    data object DiscoveryStarted : JoinMultiplayerScreenState()

    data object ConnectingToHost : JoinMultiplayerScreenState()

    data object AcceptedByHost : JoinMultiplayerScreenState()

    data object RejectedByHost : JoinMultiplayerScreenState()
}