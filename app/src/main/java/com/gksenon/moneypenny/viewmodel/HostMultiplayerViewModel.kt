package com.gksenon.moneypenny.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gksenon.moneypenny.domain.HostMatchMaker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HostMultiplayerViewModel @Inject constructor(private val matchMaker: HostMatchMaker) :
    ViewModel() {

    private val _state = MutableStateFlow(HostMultiplayerScreenState())
    val state = _state.asStateFlow()

    init {
        matchMaker.players.onEach { players ->
            _state.update { previousState -> previousState.copy(players = players) }
        }.launchIn(viewModelScope)
        matchMaker.hostAddress.onEach { hostAddress ->
            _state.update { previousState -> previousState.copy(hostAddress = hostAddress) }
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        matchMaker.reset()
    }

    fun onPermissionsGranted() {
        viewModelScope.launch { matchMaker.startAdvertising() }
    }

    fun onHostNameChanged(value: String) {
        _state.update { previousState ->
            previousState.copy(
                hostName = value,
                showHostNameIsEmptyError = false
            )
        }
    }

    fun onStartingMoneyChanged(value: String) {
        _state.update { previousState ->
            val validatedStartingMoney = value.filter { it.isDigit() }.take(9)
            previousState.copy(
                startingMoney = validatedStartingMoney,
                showStartingMoneyInvalidError = false
            )
        }
    }

    fun onAcceptPlayerButtonClicked(playerId: UUID) {
        viewModelScope.launch { matchMaker.acceptConnection(playerId) }
    }

    fun onDenyPlayerButtonClicked(playerId: UUID) {
        viewModelScope.launch { matchMaker.rejectConnection(playerId) }
    }

    fun onStartButtonClicked() {
        _state.update { previousState ->
            val isHostNameValid = previousState.hostName.isNotBlank()
            val startingMoney = previousState.startingMoney.toIntOrNull()
            val isStartingMoneyValid = startingMoney != null && startingMoney > 0
            if (isHostNameValid && isStartingMoneyValid)
                previousState.copy(showStartGameConfirmationDialog = true)
            else
                previousState.copy(
                    showHostNameIsEmptyError = !isHostNameValid,
                    showStartingMoneyInvalidError = !isStartingMoneyValid
                )
        }
    }

    fun onStartGameConfirmationDialogConfirmed(onNavigateToGameScreen: () -> Unit) {
        val hostName = _state.value.hostName
        val startingMoney = _state.value.startingMoney.toIntOrNull() ?: 0
        val players = _state.value.players
        viewModelScope.launch {
            matchMaker.startGame(hostName, startingMoney, players)
            onNavigateToGameScreen()
        }
    }

    fun onStartGameConfirmationDialogDismissed() {
        _state.update { previousState -> previousState.copy(showStartGameConfirmationDialog = false) }
    }
}

data class HostMultiplayerScreenState(
    val hostName: String = "",
    val hostAddress: String = "",
    val showHostNameIsEmptyError: Boolean = false,
    val startingMoney: String = "",
    val showStartingMoneyInvalidError: Boolean = false,
    val players: List<HostMatchMaker.Player> = emptyList(),
    val showStartGameConfirmationDialog: Boolean = false
)
