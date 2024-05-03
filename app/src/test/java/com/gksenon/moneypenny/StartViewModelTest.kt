package com.gksenon.moneypenny

import com.gksenon.moneypenny.domain.Accountant
import com.gksenon.moneypenny.domain.GameParamsValidationStatus
import com.gksenon.moneypenny.viewmodel.StartViewModel
import io.mockk.coEvery
import io.mockk.every
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

private const val PLAYER_1 = "player_1"
private const val PLAYER_2 = "player_2"

@ExperimentalCoroutinesApi
class StartViewModelTest {

    private val accountant: Accountant = mockk {
        every { validateGameParams(any(), any()) } returns GameParamsValidationStatus.VALID
    }

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

    @Test
    fun init_showsEmptyState() = runTest {
        val viewModel = StartViewModel(accountant)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.startingMoney.isEmpty())
        assertTrue(state.playerName.isEmpty())
        assertTrue(state.players.isEmpty())
        assertFalse(state.showPlayerNameIsEmptyError)
        assertFalse(state.showPlayerNameMustBeUniqueError)
        assertFalse(state.isStartButtonEnabled)
    }

    @ParameterizedTest
    @MethodSource("provideStartingMoney")
    fun onStartingMoneyChanged_validatesStartingMoney(input: String, expectedOutput: String) =
        runTest {
            val viewModel = StartViewModel(accountant)
            viewModel.onStartingMoneyChanged(input)
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals(expectedOutput, state.startingMoney)
        }

    @Test
    fun onStartingMoneyChanged_ifGameParamsAreValid_enablesStartButton() = runTest {
        val viewModel = StartViewModel(accountant)
        viewModel.onStartingMoneyChanged("2000")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isStartButtonEnabled)
    }

    @Test
    fun onStartingMoneyChanged_ifGameParamsAreInvalid_disablesStartButton() = runTest {
        every { accountant.validateGameParams(any(), any()) } returns GameParamsValidationStatus.STARTING_MONEY_IS_INVALID
        val viewModel = StartViewModel(accountant)
        viewModel.onStartingMoneyChanged("2000")
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isStartButtonEnabled)
    }

    @Test
    fun onPlayerNameChanged_changesPlayer() = runTest {
        val viewModel = StartViewModel(accountant)
        viewModel.onPlayerNameChanged(PLAYER_1)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(PLAYER_1, state.playerName)
    }

    @Test
    fun onPlayerNameChanged_ifPlayerNameIsEmptyErrorIsShown_hidesError() = runTest {
        val viewModel = StartViewModel(accountant)
        viewModel.onAddPlayerButtonClicked()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showPlayerNameIsEmptyError)

        viewModel.onPlayerNameChanged(PLAYER_1)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.showPlayerNameIsEmptyError)
    }

    @Test
    fun onPlayerNameChanged_ifPlayerMustBeUniqueErrorShown_hidesError() = runTest {
        val viewModel = StartViewModel(accountant)
        viewModel.onPlayerNameChanged(PLAYER_1)
        viewModel.onAddPlayerButtonClicked()

        every { accountant.validateGameParams(any(), any()) } returns GameParamsValidationStatus.PLAYERS_NOT_UNIQUE
        viewModel.onPlayerNameChanged(PLAYER_1)
        viewModel.onAddPlayerButtonClicked()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showPlayerNameMustBeUniqueError)

        every { accountant.validateGameParams(any(), any()) } returns GameParamsValidationStatus.VALID
        viewModel.onPlayerNameChanged(PLAYER_2)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.showPlayerNameMustBeUniqueError)
    }

    @Test
    fun onAddPlayerButtonClicked_ifPlayerNameIsEmpty_showsError() = runTest {
        val viewModel = StartViewModel(accountant)
        viewModel.onAddPlayerButtonClicked()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.players.isEmpty())
        assertTrue(state.showPlayerNameIsEmptyError)
    }

    @Test
    fun onAddPlayerButtonClicked_ifPlayerIsDuplicate_showsError() = runTest {
        val viewModel = StartViewModel(accountant)
        viewModel.onPlayerNameChanged(PLAYER_1)
        viewModel.onAddPlayerButtonClicked()

        every { accountant.validateGameParams(any(), any()) } returns GameParamsValidationStatus.PLAYERS_NOT_UNIQUE

        viewModel.onPlayerNameChanged(PLAYER_1)
        viewModel.onAddPlayerButtonClicked()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(listOf(PLAYER_1), state.players)
        assertTrue(state.showPlayerNameMustBeUniqueError)
    }

    @Test
    fun onAddPlayerButtonClicked_ifPlayerNameIsValid_addsPlayer() = runTest {
        val viewModel = StartViewModel(accountant)
        viewModel.onPlayerNameChanged(PLAYER_1)
        viewModel.onAddPlayerButtonClicked()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(listOf(PLAYER_1), state.players)
        assertTrue(state.playerName.isEmpty())
    }

    @Test
    fun onAddPlayerButtonClicked_ifGameParamsAreValid_enablesStartButton() = runTest {
//        every { accountant.areGameParamsValid(any(), any()) } returns true
        val viewModel = StartViewModel(accountant)
        viewModel.onPlayerNameChanged(PLAYER_1)
        viewModel.onAddPlayerButtonClicked()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isStartButtonEnabled)
    }

    @Test
    fun onAddPlayerButtonClicked_ifGameParamsAreInvalid_disablesStartButton() = runTest {
        every { accountant.validateGameParams(any(), any()) } returns GameParamsValidationStatus.PLAYERS_AMOUNT_IS_INVALID
        val viewModel = StartViewModel(accountant)
        viewModel.onPlayerNameChanged(PLAYER_1)
        viewModel.onAddPlayerButtonClicked()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isStartButtonEnabled)
    }

    @Test
    fun onDeletePlayerButtonClicked_deletesPlayer() = runTest {
        val viewModel = StartViewModel(accountant)
        viewModel.onPlayerNameChanged(PLAYER_1)
        viewModel.onAddPlayerButtonClicked()
        viewModel.onPlayerNameChanged(PLAYER_2)
        viewModel.onAddPlayerButtonClicked()
        viewModel.onDeletePlayerButtonClicked(PLAYER_1)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(listOf(PLAYER_2), state.players)
    }

    @Test
    fun onDeletePlayerButtonClicked_ifGameParamsAreValid_enablesStartButton() = runTest {
//        every { accountant.areGameParamsValid(any(), any()) } returns true
        val viewModel = StartViewModel(accountant)
        viewModel.onPlayerNameChanged(PLAYER_1)
        viewModel.onAddPlayerButtonClicked()
        viewModel.onDeletePlayerButtonClicked(PLAYER_1)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isStartButtonEnabled)
    }

    @Test
    fun onDeletePlayerButtonClicked_ifGameParamsAreInvalid_disablesStartButton() = runTest {
        every { accountant.validateGameParams(any(), any()) } returns GameParamsValidationStatus.PLAYERS_AMOUNT_IS_INVALID
        val viewModel = StartViewModel(accountant)
        viewModel.onPlayerNameChanged(PLAYER_1)
        viewModel.onAddPlayerButtonClicked()
        viewModel.onDeletePlayerButtonClicked(PLAYER_1)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isStartButtonEnabled)
    }

    @Test
    fun onStartButtonClicked_startsGame() = runTest {
        val startingMoneySlot = slot<Int>()
        val playersSlot = slot<List<String>>()
        coEvery {
            accountant.startGame(
                capture(startingMoneySlot),
                capture(playersSlot)
            )
        } returns Unit
        val viewModel = StartViewModel(accountant)
        viewModel.onPlayerNameChanged(PLAYER_1)
        viewModel.onAddPlayerButtonClicked()
        viewModel.onPlayerNameChanged(PLAYER_2)
        viewModel.onAddPlayerButtonClicked()
        viewModel.onStartingMoneyChanged("2000")
        viewModel.onStartButtonClicked()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.startingMoney.isEmpty())
        assertTrue(state.playerName.isEmpty())
        assertTrue(state.players.isEmpty())
        assertFalse(state.showPlayerNameIsEmptyError)
        assertFalse(state.isStartButtonEnabled)
        assertEquals(2000, startingMoneySlot.captured)
        assertEquals(listOf(PLAYER_1, PLAYER_2), playersSlot.captured)
    }
}