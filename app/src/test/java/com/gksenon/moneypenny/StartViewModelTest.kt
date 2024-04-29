package com.gksenon.moneypenny

import com.gksenon.moneypenny.domain.Accountant
import com.gksenon.moneypenny.viewmodel.StartViewModel
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

private const val PLAYER_NAME = "player"

@ExperimentalCoroutinesApi
class StartViewModelTest {

    private val accountant: Accountant = mockk()

    companion object {

        @JvmStatic
        fun provideStartingMoney() = listOf(
            Arguments.of("123", "123"),
            Arguments.of("-123", "123"),
            Arguments.of("12345678910", "123456789"),
            Arguments.of("5t6y7v.-/ft6re", "5676")
        )
    }

    @BeforeEach
    fun before() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterEach
    fun after() {
        Dispatchers.resetMain()
    }

    @ParameterizedTest
    @MethodSource("provideStartingMoney")
    fun onStartingMoneyChanged_validatesStartingMoney(input: String, expectedOutput: String) = runTest {
        val viewModel = StartViewModel(accountant)
        advanceUntilIdle()

        viewModel.onStartingMoneyChanged(input)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(expectedOutput, state.startingMoney)
    }

    @Test
    fun onPlayerNameChanged_changesPlayer() = runTest {
        val viewModel = StartViewModel(accountant)
        advanceUntilIdle()

        viewModel.onPlayerNameChanged(PLAYER_NAME)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(PLAYER_NAME, state.playerName)
    }

    @Test
    fun onAddPlayerButtonClicked_ifPlayerNameIsEmpty_doesNothing() = runTest {
        val viewModel = StartViewModel(accountant)
        advanceUntilIdle()

        viewModel.onAddPlayerButtonClicked()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.players.isEmpty())
    }

    @Test
    fun onAddPlayerButtonClicked_ifPLayerNameIsNotEmpty_addsPlayer() = runTest {
        val viewModel = StartViewModel(accountant)
        advanceUntilIdle()

        viewModel.onPlayerNameChanged(PLAYER_NAME)
        advanceUntilIdle()

        viewModel.onAddPlayerButtonClicked()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(listOf(PLAYER_NAME), state.players)
        assertEquals("", state.playerName)
        assertFalse(state.showPlayersListIsEmptyError)
    }

    @Test
    fun onDeletePlayerButtonClicked_deletesPlayer() = runTest {
        val viewModel = StartViewModel(accountant)
        advanceUntilIdle()
        viewModel.onPlayerNameChanged(PLAYER_NAME)
        advanceUntilIdle()
        viewModel.onAddPlayerButtonClicked()
        advanceUntilIdle()

        viewModel.onDeletePlayerButtonClicked(PLAYER_NAME)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.players.isEmpty())
    }

    @Test
    fun onStartButtonClicked_ifStartingMoneyIsInvalid_showsError() = runTest {
        val viewModel = StartViewModel(accountant)
        advanceUntilIdle()

        viewModel.onPlayerNameChanged(PLAYER_NAME)
        advanceUntilIdle()

        viewModel.onAddPlayerButtonClicked()
        advanceUntilIdle()

        viewModel.onStartingMoneyChanged("0")
        advanceUntilIdle()

        viewModel.onStartButtonClicked()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.showStartingMoneyInvalidError)
    }

    @Test
    fun onStartButtonClicked_ifPlayersListIsEmpty_showsError() = runTest {
        val viewModel = StartViewModel(accountant)
        advanceUntilIdle()

        viewModel.onStartingMoneyChanged("2000")
        advanceUntilIdle()

        viewModel.onStartButtonClicked()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.showPlayersListIsEmptyError)
    }

    @Test
    fun onStartButtonClicked_startsGame() = runTest {
        val startingMoneySlot = slot<Int>()
        val playersSlot = slot<List<String>>()
        coEvery { accountant.startGame(capture(startingMoneySlot), capture(playersSlot)) } returns Unit
        val viewModel = StartViewModel(accountant)
        advanceUntilIdle()

        viewModel.onPlayerNameChanged(PLAYER_NAME)
        advanceUntilIdle()

        viewModel.onAddPlayerButtonClicked()
        advanceUntilIdle()

        viewModel.onStartingMoneyChanged("2000")
        advanceUntilIdle()

        viewModel.onStartButtonClicked()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.startingMoney.isEmpty())
        assertTrue(state.playerName.isEmpty())
        assertTrue(state.players.isEmpty())
        assertFalse(state.showStartingMoneyInvalidError)
        assertFalse(state.showPlayersListIsEmptyError)
        assertEquals(2000, startingMoneySlot.captured)
        assertEquals(listOf(PLAYER_NAME), playersSlot.captured)
    }
}