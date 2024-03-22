package com.gksenon.moneypenny.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gksenon.moneypenny.domain.Accountant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartViewModel @Inject constructor(private val accountant: Accountant) : ViewModel() {

    private val _state = MutableStateFlow(StartScreenState())
    val state = _state.asStateFlow()
    fun onStartingMoneyChanged(value: String) {
        _state.update {
            val startingMoney = value.filter { c -> c.isDigit() }
            it.copy(
                startingMoney = startingMoney,
                showStartingMoneyInvalidError = false
            )
        }
    }

    fun onPlayerNameChanged(name: String) {
        _state.update { it.copy(playerName = name) }
    }

    fun onAddPlayerButtonClicked() {
        val currentState = _state.value
        if (currentState.playerName.isNotEmpty()) {
            val players = currentState.players.plus(currentState.playerName)
            _state.update {
                it.copy(playerName = "", players = players, showPlayersListIsEmptyError = false)
            }
        }
    }

    fun onStartButtonClicked() {
        val currentState = _state.value
        val startingMoney = currentState.startingMoney.toIntOrNull()
        val players = currentState.players
        when {
            startingMoney == null -> _state.update { it.copy(showStartingMoneyInvalidError = true) }
            players.isEmpty() -> _state.update { currentState.copy(showPlayersListIsEmptyError = true) }
            else -> {
                _state.update { StartScreenState() }
                viewModelScope.launch { accountant.startGame(startingMoney, players) }
            }
        }
    }
}

data class StartScreenState(
    val startingMoney: String = "",
    val showStartingMoneyInvalidError: Boolean = false,
    val playerName: String = "",
    val players: List<String> = emptyList(),
    val showPlayersListIsEmptyError: Boolean = false
)