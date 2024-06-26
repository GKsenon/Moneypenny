package com.gksenon.moneypenny.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gksenon.moneypenny.domain.Accountant
import com.gksenon.moneypenny.domain.GameParamsValidationError
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
        _state.update { previousState ->
            val startingMoney = value.filter { c -> c.isDigit() }.take(9)
            val parsedStartingMoney = startingMoney.toIntOrNull() ?: 0
            val validationErrors =
                accountant.validateGameParams(parsedStartingMoney, previousState.players)
            previousState.copy(
                startingMoney = startingMoney,
                isStartButtonEnabled = validationErrors.isEmpty()
            )
        }
    }

    fun onPlayerNameChanged(name: String) {
        _state.update {
            it.copy(
                playerName = name,
                showPlayerNameIsEmptyError = false,
                showPlayerNameMustBeUniqueError = false
            )
        }
    }

    fun onAddPlayerButtonClicked() {
        val startingMoney = _state.value.startingMoney.toIntOrNull() ?: 0
        val playerName = _state.value.playerName
        val players = _state.value.players.plus(playerName)
        val validationErrors = accountant.validateGameParams(startingMoney, players)
        when {
            playerName.isEmpty() -> {
                _state.update { previousState ->
                    previousState.copy(showPlayerNameIsEmptyError = true)
                }
            }

            validationErrors.contains(GameParamsValidationError.PLAYERS_NOT_UNIQUE) -> {
                _state.update { previousState ->
                    previousState.copy(showPlayerNameMustBeUniqueError = true)
                }
            }

            else -> {
                _state.update { previousState ->
                    previousState.copy(
                        playerName = "",
                        players = players,
                        isStartButtonEnabled = validationErrors.isEmpty()
                    )
                }
            }
        }
    }

    fun onDeletePlayerButtonClicked(playerName: String) {
        _state.update { previousState ->
            val players = previousState.players.minus(playerName)
            val startingMoney = previousState.startingMoney.toIntOrNull() ?: 0
            val validationErrors = accountant.validateGameParams(startingMoney, players)
            previousState.copy(
                players = players,
                isStartButtonEnabled = validationErrors.isEmpty()
            )
        }
    }

    fun onStartButtonClicked() {
        val currentState = _state.value
        val startingMoney = currentState.startingMoney.toIntOrNull() ?: 0
        val players = currentState.players
        _state.update { StartScreenState() }
        viewModelScope.launch { accountant.startGame(startingMoney, players) }
    }
}

data class StartScreenState(
    val startingMoney: String = "",
    val playerName: String = "",
    val showPlayerNameIsEmptyError: Boolean = false,
    val showPlayerNameMustBeUniqueError: Boolean = false,
    val players: List<String> = emptyList(),
    val isStartButtonEnabled: Boolean = false
)