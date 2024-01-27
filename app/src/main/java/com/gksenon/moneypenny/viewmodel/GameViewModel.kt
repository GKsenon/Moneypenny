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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(private val accountant: Accountant) : ViewModel() {

    private val _state = MutableStateFlow<GameScreenState>(GameScreenState.GameNotStarted())
    val state = _state.asStateFlow()

    init {
        accountant.getTransactionHistory().onEach { transactionHistory ->
            val state = if (transactionHistory.isEmpty()) {
                GameScreenState.GameNotStarted()
            } else {
                GameScreenState.GameInProgress(
                    balance = transactionHistory.sumOf { it.amount },
                    transactionHistory = transactionHistory.sortedByDescending { it.time }.map { it.amount }
                )
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
            viewModelScope.launch { accountant.startGame(startingMoney) }
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
        val currentState = _state.value as GameScreenState.GameInProgress
        viewModelScope.launch { accountant.add(currentState.moneyValue.toIntOrNull() ?: 0) }
        _state.update { previousState ->
            (previousState as GameScreenState.GameInProgress).copy(
                showAddMoneyDialog = false,
                moneyValue = ""
            )
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
        val currentState = _state.value as GameScreenState.GameInProgress
        viewModelScope.launch { accountant.subtract(currentState.moneyValue.toIntOrNull() ?: 0) }
        _state.update { previousState ->
            (previousState as GameScreenState.GameInProgress).copy(
                showSubtractMoneyDialog = false,
                moneyValue = ""
            )
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
        viewModelScope.launch { accountant.finishGame() }
    }
}

sealed class GameScreenState {
    data class GameNotStarted(
        val startingMoney: String = "",
        val showStartingMoneyInvalidError: Boolean = false
    ) : GameScreenState()

    data class GameInProgress(
        val balance: Int = 0,
        val transactionHistory: List<Int> = emptyList(),
        val showAddMoneyDialog: Boolean = false,
        val showSubtractMoneyDialog: Boolean = false,
        val moneyValue: String = ""
    ) : GameScreenState()
}
