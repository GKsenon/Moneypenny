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
class MainViewModel @Inject constructor(accountant: Accountant) : ViewModel() {

    private val _state = MutableStateFlow(MainScreenState.START_GAME)
    val state = _state.asStateFlow()

    init {
        accountant.isGameStarted.onEach { started ->
            _state.update {
                if (started)
                    MainScreenState.GAME_IN_PROGRESS
                else
                    MainScreenState.START_GAME
            }
        }.launchIn(viewModelScope)
    }
}

enum class MainScreenState {
    START_GAME, GAME_IN_PROGRESS
}