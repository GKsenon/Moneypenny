package com.gksenon.moneypenny.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.gksenon.moneypenny.viewmodel.MainScreenState
import com.gksenon.moneypenny.viewmodel.MainViewModel

@Composable
@ExperimentalMaterial3Api
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    when (state) {
        MainScreenState.START_GAME -> StartScreen()
        MainScreenState.GAME_IN_PROGRESS -> GameScreen()
    }
}

