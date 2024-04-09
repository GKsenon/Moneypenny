package com.gksenon.moneypenny

import com.gksenon.moneypenny.domain.Accountant
import com.gksenon.moneypenny.viewmodel.MainScreenState
import com.gksenon.moneypenny.viewmodel.MainViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class MainViewModelTest {

    private val gameStateFlow = MutableStateFlow(false)
    private val accountant = mockk<Accountant> {
        every { isGameStarted } returns gameStateFlow
    }

    @BeforeEach
    fun before() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterEach
    fun after() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_ifGameNotStarted_showsStartScreen() = runTest {
        val viewModel = MainViewModel(accountant)
        advanceUntilIdle()

        assertEquals(MainScreenState.START_GAME, viewModel.state.value)
    }

    @Test
    fun init_ifGameStarted_showsGameScreen() = runTest {
        gameStateFlow.value = true
        val viewModel = MainViewModel(accountant)
        advanceUntilIdle()

        assertEquals(MainScreenState.GAME_IN_PROGRESS, viewModel.state.value)
    }

    @Test
    fun init_onGameStateChanged_changesScreen() = runTest {
        val viewModel = MainViewModel(accountant)
        advanceUntilIdle()

        gameStateFlow.value = true
        advanceUntilIdle()
        assertEquals(MainScreenState.GAME_IN_PROGRESS, viewModel.state.value)

        gameStateFlow.value = false
        advanceUntilIdle()
        assertEquals(MainScreenState.START_GAME, viewModel.state.value)
    }
}