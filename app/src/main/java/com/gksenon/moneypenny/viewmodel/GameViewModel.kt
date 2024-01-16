package com.gksenon.moneypenny.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gksenon.moneypenny.domain.Accountant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(private val accountant: Accountant) : ViewModel() {

    private val _state = MutableStateFlow<GameScreenState>(GameScreenState.GameNotStarted())
    val state = _state.asStateFlow()

    init {
        accountant.getBalance().onEach { balance ->
            val state = if (balance == null) {
                GameScreenState.GameNotStarted()
            } else {
                GameScreenState.GameInProgress(balance)
            }
            _state.update { state }
        }.launchIn(viewModelScope)
    }

    fun onStartingMoneyChanged(value: String) {
        _state.update {
            val startingMoney = value.filter { it.isDigit() }
            GameScreenState.GameNotStarted(
                startingMoney = startingMoney,
                showStartingMoneyInvalidError = false
            )
        }
    }

    fun onStartButtonClicked() {
        val currentState = _state.value as GameScreenState.GameNotStarted
        val startingMoney = currentState.startingMoney.toIntOrNull()
        if (startingMoney != null) {
            accountant.startGame(startingMoney)
        } else {
            _state.update { currentState.copy(showStartingMoneyInvalidError = true) }
        }
    }

    fun onAddButtonClicked() {
        _state.update { currentState ->
            (currentState as GameScreenState.GameInProgress).copy(showAddMoneyDialog = true)
        }
    }

    fun onAddDialogConfirmed() {
        _state.update {
            val state = it as GameScreenState.GameInProgress
            accountant.add(state.moneyValue.toIntOrNull() ?: 0)
            state.copy(showAddMoneyDialog = false, moneyValue = "")
        }
    }

    fun onAddDialogDismissed() {
        _state.update { currentState ->
            (currentState as GameScreenState.GameInProgress).copy(
                showAddMoneyDialog = false,
                moneyValue = ""
            )
        }
    }

    fun onSubtractButtonClicked() {
        _state.update { currentState ->
            (currentState as GameScreenState.GameInProgress).copy(showSubtractMoneyDialog = true)
        }
    }

    fun onSubtractDialogConfirmed() {
        _state.update {
            val state = it as GameScreenState.GameInProgress
            accountant.subtract(state.moneyValue.toIntOrNull() ?: 0)
            state.copy(showSubtractMoneyDialog = false, moneyValue = "")
        }
    }

    fun onSubtractDialogDismissed() {
        _state.update { currentState ->
            (currentState as GameScreenState.GameInProgress).copy(showSubtractMoneyDialog = false)
        }
    }

    fun onMoneyValueChanged(value: String) {
        _state.update { currentState ->
            val validatedValue = value.filter { it.isDigit() }.take(9)
            (currentState as GameScreenState.GameInProgress).copy(moneyValue = validatedValue)
        }
    }

    fun onFinishGameClicked() {
        accountant.finishGame()
    }
}

sealed class GameScreenState {
    data class GameNotStarted(
        val startingMoney: String = "",
        val showStartingMoneyInvalidError: Boolean = false
    ) : GameScreenState()

    data class GameInProgress(
        val balance: Int = 0,
        val showAddMoneyDialog: Boolean = false,
        val showSubtractMoneyDialog: Boolean = false,
        val moneyValue: String = ""
    ) : GameScreenState()
}
