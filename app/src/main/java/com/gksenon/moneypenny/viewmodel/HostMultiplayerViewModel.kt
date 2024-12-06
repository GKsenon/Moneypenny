package com.gksenon.moneypenny.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gksenon.moneypenny.domain.Accountant
import com.gksenon.moneypenny.domain.HostMatchMaker
import com.gksenon.moneypenny.domain.MULTIPLAYER_HOST_GAME
import com.gksenon.moneypenny.domain.dto.PlayerDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class HostMultiplayerViewModel @Inject constructor(
    private val matchMaker: HostMatchMaker,
    @Named(MULTIPLAYER_HOST_GAME) private val accountant: Accountant
) : ViewModel() {

    private val _state = MutableStateFlow(HostMultiplayerScreenState())
    val state = _state.asStateFlow()

    init {
        matchMaker.players.onEach { players ->
            _state.update { previousState -> previousState.copy(players = players) }
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

    fun onAcceptPlayerButtonClicked(connectionId: String) {
        matchMaker.acceptConnection(connectionId)
    }

    fun onDenyPlayerButtonClicked(connectionId: String) {
        matchMaker.rejectConnection(connectionId)
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
        matchMaker.reset()
        val startingMoney = _state.value.startingMoney.toIntOrNull() ?: 0
        val players = _state.value.players.map { PlayerDto(it.id, it.name) }
        accountant.startGame(startingMoney, players)
        onNavigateToGameScreen()
    }

    fun onStartGameConfirmationDialogDismissed() {
        _state.update { previousState -> previousState.copy(showStartGameConfirmationDialog = false) }
    }
}

data class HostMultiplayerScreenState(
    val hostName: String = "",
    val showHostNameIsEmptyError: Boolean = false,
    val startingMoney: String = "",
    val showStartingMoneyInvalidError: Boolean = false,
    val players: List<HostMatchMaker.Player> = emptyList(),
    val showStartGameConfirmationDialog: Boolean = false
)
