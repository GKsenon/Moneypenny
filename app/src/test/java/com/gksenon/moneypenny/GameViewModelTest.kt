package com.gksenon.moneypenny

import com.gksenon.moneypenny.data.InMemoryAccountant
import com.gksenon.moneypenny.domain.Accountant
import com.gksenon.moneypenny.viewmodel.GameScreenState
import com.gksenon.moneypenny.viewmodel.GameViewModel
import io.mockk.spyk
import io.mockk.verify
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

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private val accountant: Accountant = spyk(InMemoryAccountant())
    private val testDispatcher = StandardTestDispatcher()

    companion object {

        @JvmStatic
        fun provideStartingMoney() = listOf(
            Arguments.of("", ""),
            Arguments.of("20ghfh56", "2056"),
            Arguments.of("-1500", "1500"),
        )
    }

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun clear() {
        Dispatchers.resetMain()
    }

    @Test
    fun gameViewModel_initWithoutStartingMoney_showsStartGameState() = runTest {
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameNotStarted
        assertEquals("", state.startingMoney)
        verify { accountant.getBalance() }
    }

    @Test
    fun gameViewModel_initWithStartingMoney_showsGameInProgressState() = runTest {
        accountant.startGame(1500)
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameInProgress
        assertEquals(1500, state.balance)
        assertFalse(state.showAddMoneyDialog)
        assertFalse(state.showSubtractMoneyDialog)
        verify { accountant.getBalance() }
    }

    @ParameterizedTest
    @MethodSource("provideStartingMoney")
    fun gameViewModel_onStartingMoneyChanged_shouldValidateValue(
        input: String,
        expectedOutput: String
    ) = runTest {
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()
        viewModel.onStartingMoneyChanged(input)
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameNotStarted
        assertEquals(expectedOutput, state.startingMoney)
    }

    @Test
    fun gameViewModel_onStartButtonClickedWithInvalidMoney_shouldShowError() = runTest {
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()
        viewModel.onStartingMoneyChanged("")
        advanceUntilIdle()
        viewModel.onStartButtonClicked()
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameNotStarted
        assertTrue(state.showStartingMoneyInvalidError)
    }
    @Test
    fun gameViewModel_onStartButtonClicked_shouldStartGame() = runTest {
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()
        viewModel.onStartingMoneyChanged("1500")
        advanceUntilIdle()
        viewModel.onStartButtonClicked()
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameInProgress
        assertEquals(1500, state.balance)
        assertFalse(state.showAddMoneyDialog)
        assertFalse(state.showSubtractMoneyDialog)
        verify { accountant.startGame(1500) }
    }

    @Test
    fun gameViewModel_onAddButtonClicked_shouldShowAddDialog() = runTest {
        accountant.startGame(1500)
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()
        viewModel.onAddButtonClicked()
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameInProgress
        assertTrue(state.showAddMoneyDialog)
        assertFalse(state.showSubtractMoneyDialog)
    }

    @Test
    fun gameViewModel_onMoneyValueChanged_shouldValidateValue() = runTest {
        accountant.startGame(1500)
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()
        viewModel.onAddButtonClicked()
        advanceUntilIdle()
        viewModel.onMoneyValueChanged("rirnf993939393ee9999")
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameInProgress
        assertEquals("993939393", state.moneyValue)
    }

    @Test
    fun gameViewModel_onAddDialogConfirmed_shouldAddMoney() = runTest {
        accountant.startGame(1500)
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()
        viewModel.onAddButtonClicked()
        advanceUntilIdle()
        viewModel.onMoneyValueChanged("1000")
        advanceUntilIdle()
        viewModel.onAddDialogConfirmed()
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameInProgress
        assertEquals(2500, state.balance)
        assertFalse(state.showAddMoneyDialog)
        assertFalse(state.showSubtractMoneyDialog)
        verify { accountant.add(1000) }
    }

    @Test
    fun gameViewModel_onAddDialogDismissed_shouldCloseDialog() = runTest {
        accountant.startGame(1500)
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()
        viewModel.onAddButtonClicked()
        advanceUntilIdle()
        viewModel.onMoneyValueChanged("1000")
        advanceUntilIdle()
        viewModel.onAddDialogDismissed()
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameInProgress
        assertEquals(1500, state.balance)
        assertFalse(state.showAddMoneyDialog)
        assertFalse(state.showSubtractMoneyDialog)
    }

    @Test
    fun gameViewModel_onSubtractButtonClicked_shouldShowSubtractDialog() = runTest {
        accountant.startGame(1500)
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()
        viewModel.onSubtractButtonClicked()
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameInProgress
        assertTrue(state.showSubtractMoneyDialog)
        assertFalse(state.showAddMoneyDialog)
    }

    @Test
    fun gameViewModel_onSubtractDialogConfirmed_shouldSubtractMoney() = runTest {
        accountant.startGame(1500)
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()
        viewModel.onSubtractButtonClicked()
        advanceUntilIdle()
        viewModel.onMoneyValueChanged("500")
        advanceUntilIdle()
        viewModel.onSubtractDialogConfirmed()
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameInProgress
        assertEquals(1000, state.balance)
        assertFalse(state.showAddMoneyDialog)
        assertFalse(state.showSubtractMoneyDialog)
        verify { accountant.subtract(500) }
    }

    @Test
    fun gameViewModel_onSubtractDialogDismissed_shouldCloseDialog() = runTest {
        accountant.startGame(1500)
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()
        viewModel.onSubtractButtonClicked()
        advanceUntilIdle()
        viewModel.onMoneyValueChanged("500")
        advanceUntilIdle()
        viewModel.onSubtractDialogDismissed()
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameInProgress
        assertEquals(1500, state.balance)
        assertFalse(state.showAddMoneyDialog)
        assertFalse(state.showSubtractMoneyDialog)
    }

    @Test
    fun gameViewModel_onFinishGameButtonClicked_shouldFinishGame() = runTest {
        accountant.startGame(1500)
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()
        viewModel.onFinishGameClicked()
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameNotStarted
        assertEquals("", state.startingMoney)
        verify { accountant.finishGame() }
    }
}