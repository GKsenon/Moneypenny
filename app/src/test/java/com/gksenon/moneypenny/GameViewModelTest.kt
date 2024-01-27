package com.gksenon.moneypenny

import com.gksenon.moneypenny.domain.Accountant
import com.gksenon.moneypenny.domain.Transaction
import com.gksenon.moneypenny.viewmodel.GameScreenState
import com.gksenon.moneypenny.viewmodel.GameViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.UUID

private const val STARTING_MONEY = 1500

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private val accountant: Accountant = mockk()
    private val testDispatcher = StandardTestDispatcher()

    companion object {

        @JvmStatic
        fun provideStartingMoney() = listOf(
            Arguments.of("", ""),
            Arguments.of("20ghfh56", "2056"),
            Arguments.of("-1500", "1500"),
        )

        @JvmStatic
        fun provideMoneyValueValidationArguments() = listOf(
            Arguments.of("", ""),
            Arguments.of("123456789011", "123456789"),
            Arguments.of("1234ghfhfh56", "123456")
        )

        @JvmStatic
        fun provideAddDialogConfirmationArguments() = listOf(
            Arguments.of("", 0, listOf(STARTING_MONEY), STARTING_MONEY),
            Arguments.of(
                "999999999",
                999999999,
                listOf(999999999, STARTING_MONEY),
                STARTING_MONEY + 999999999
            ),
            Arguments.of("2500", 2500, listOf(2500, STARTING_MONEY), STARTING_MONEY + 2500)
        )

        @JvmStatic
        fun provideSubtractDialogConfirmationArguments() = listOf(
            Arguments.of("", 0, listOf(STARTING_MONEY), STARTING_MONEY),
            Arguments.of(
                "999999999",
                999999999,
                listOf(-999999999, STARTING_MONEY),
                STARTING_MONEY - 999999999
            ),
            Arguments.of("2500", 2500, listOf(-2500, STARTING_MONEY), STARTING_MONEY - 2500)
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
        every { accountant.getTransactionHistory() } returns flowOf(emptyList())
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameNotStarted
        assertEquals("", state.startingMoney)
        verify { accountant.getTransactionHistory() }
    }

    @Test
    fun gameViewModel_initWithStartingMoney_showsGameInProgressState() = runTest {
        every { accountant.getTransactionHistory() } returns flowOf(
            listOf(
                Transaction(
                    UUID.randomUUID(),
                    Instant.now(),
                    STARTING_MONEY
                )
            )
        )
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameInProgress
        assertEquals(STARTING_MONEY, state.balance)
        assertFalse(state.showAddMoneyDialog)
        assertFalse(state.showSubtractMoneyDialog)
        verify { accountant.getTransactionHistory() }
    }

    @ParameterizedTest
    @MethodSource("provideStartingMoney")
    fun gameViewModel_onStartingMoneyChanged_shouldValidateValue(
        input: String,
        expectedOutput: String
    ) = runTest {
        every { accountant.getTransactionHistory() } returns flowOf(emptyList())
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()
        viewModel.onStartingMoneyChanged(input)
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameNotStarted
        assertEquals(expectedOutput, state.startingMoney)
    }

    @Test
    fun gameViewModel_onStartButtonClickedWithInvalidMoney_shouldShowError() = runTest {
        every { accountant.getTransactionHistory() } returns flowOf(emptyList())
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
        val transactionsFlow: MutableStateFlow<List<Transaction>> = MutableStateFlow(emptyList())
        every { accountant.getTransactionHistory() } returns transactionsFlow
        coEvery { accountant.startGame(STARTING_MONEY) } returns Unit
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()
        viewModel.onStartingMoneyChanged(STARTING_MONEY.toString())
        advanceUntilIdle()
        viewModel.onStartButtonClicked()
        advanceUntilIdle()

        transactionsFlow.value =
            listOf(Transaction(UUID.randomUUID(), Instant.now(), STARTING_MONEY))
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameInProgress
        assertEquals(STARTING_MONEY, state.balance)
        assertFalse(state.showAddMoneyDialog)
        assertFalse(state.showSubtractMoneyDialog)
        coVerify { accountant.startGame(STARTING_MONEY) }
    }

    @Test
    fun gameViewModel_onAddButtonClicked_shouldShowAddDialog() = runTest {
        every { accountant.getTransactionHistory() } returns flowOf(
            listOf(
                Transaction(
                    UUID.randomUUID(),
                    Instant.now(),
                    STARTING_MONEY
                )
            )
        )
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()
        viewModel.onAddButtonClicked()
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameInProgress
        assertTrue(state.showAddMoneyDialog)
        assertFalse(state.showSubtractMoneyDialog)
    }

    @ParameterizedTest
    @MethodSource("provideMoneyValueValidationArguments")
    fun gameViewModel_onMoneyValueChanged_shouldValidateValue(input: String, output: String) =
        runTest {
            every { accountant.getTransactionHistory() } returns flowOf(
                listOf(
                    Transaction(
                        UUID.randomUUID(),
                        Instant.now(),
                        STARTING_MONEY
                    )
                )
            )
            val viewModel = GameViewModel(accountant)
            advanceUntilIdle()
            viewModel.onAddButtonClicked()
            advanceUntilIdle()
            viewModel.onMoneyValueChanged(input)
            advanceUntilIdle()

            val state = viewModel.state.value as GameScreenState.GameInProgress
            assertEquals(output, state.moneyValue)
        }

    @ParameterizedTest
    @MethodSource("provideAddDialogConfirmationArguments")
    fun gameViewModel_onAddDialogConfirmed_shouldAddMoney(
        input: String,
        validatedInput: Int,
        transactions: List<Int>,
        balance: Int
    ) = runTest {
        val transactionsFlow: MutableStateFlow<List<Transaction>> =
            MutableStateFlow(listOf(Transaction(UUID.randomUUID(), Instant.now(), STARTING_MONEY)))
        every { accountant.getTransactionHistory() } returns transactionsFlow
        coEvery { accountant.add(validatedInput) } returns Unit
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()
        viewModel.onAddButtonClicked()
        advanceUntilIdle()
        viewModel.onMoneyValueChanged(input)
        advanceUntilIdle()
        viewModel.onAddDialogConfirmed()
        advanceUntilIdle()

        transactionsFlow.value =
            transactions.map { Transaction(UUID.randomUUID(), Instant.now(), it) }
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameInProgress
        assertEquals(balance, state.balance)
        assertFalse(state.showAddMoneyDialog)
        assertFalse(state.showSubtractMoneyDialog)
        assertEquals(transactions, state.transactionHistory)
        coVerify { accountant.add(validatedInput) }
    }

    @Test
    fun gameViewModel_onAddDialogDismissed_shouldCloseDialog() = runTest {
        every { accountant.getTransactionHistory() } returns flowOf(
            listOf(
                Transaction(
                    UUID.randomUUID(),
                    Instant.now(),
                    STARTING_MONEY
                )
            )
        )
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()
        viewModel.onAddButtonClicked()
        advanceUntilIdle()
        viewModel.onMoneyValueChanged("1000")
        advanceUntilIdle()
        viewModel.onAddDialogDismissed()
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameInProgress
        assertEquals(STARTING_MONEY, state.balance)
        assertFalse(state.showAddMoneyDialog)
        assertFalse(state.showSubtractMoneyDialog)
    }

    @Test
    fun gameViewModel_onSubtractButtonClicked_shouldShowSubtractDialog() = runTest {
        every { accountant.getTransactionHistory() } returns flowOf(
            listOf(
                Transaction(
                    UUID.randomUUID(),
                    Instant.now(),
                    STARTING_MONEY
                )
            )
        )
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()
        viewModel.onSubtractButtonClicked()
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameInProgress
        assertTrue(state.showSubtractMoneyDialog)
        assertFalse(state.showAddMoneyDialog)
    }

    @ParameterizedTest
    @MethodSource("provideSubtractDialogConfirmationArguments")
    fun gameViewModel_onSubtractDialogConfirmed_shouldSubtractMoney(
        input: String,
        validatedInput: Int,
        transactions: List<Int>,
        balance: Int
    ) = runTest {
        val transactionsFlow: MutableStateFlow<List<Transaction>> =
            MutableStateFlow(listOf(Transaction(UUID.randomUUID(), Instant.now(), STARTING_MONEY)))
        every { accountant.getTransactionHistory() } returns transactionsFlow
        coEvery { accountant.subtract(validatedInput) } returns Unit
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()
        viewModel.onSubtractButtonClicked()
        advanceUntilIdle()
        viewModel.onMoneyValueChanged(input)
        advanceUntilIdle()
        viewModel.onSubtractDialogConfirmed()
        advanceUntilIdle()

        transactionsFlow.value =
            transactions.map { Transaction(UUID.randomUUID(), Instant.now(), it) }
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameInProgress
        assertEquals(balance, state.balance)
        assertFalse(state.showAddMoneyDialog)
        assertFalse(state.showSubtractMoneyDialog)
        assertEquals(transactions, state.transactionHistory)
        coVerify { accountant.subtract(validatedInput) }
    }

    @Test
    fun gameViewModel_onSubtractDialogDismissed_shouldCloseDialog() = runTest {
        every { accountant.getTransactionHistory() } returns flowOf(
            listOf(
                Transaction(
                    UUID.randomUUID(),
                    Instant.now(),
                    STARTING_MONEY
                )
            )
        )
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()
        viewModel.onSubtractButtonClicked()
        advanceUntilIdle()
        viewModel.onMoneyValueChanged("500")
        advanceUntilIdle()
        viewModel.onSubtractDialogDismissed()
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameInProgress
        assertEquals(STARTING_MONEY, state.balance)
        assertFalse(state.showAddMoneyDialog)
        assertFalse(state.showSubtractMoneyDialog)
    }

    @Test
    fun gameViewModel_onFinishGameButtonClicked_shouldFinishGame() = runTest {
        val transactionsFlow: MutableStateFlow<List<Transaction>> =
            MutableStateFlow(listOf(Transaction(UUID.randomUUID(), Instant.now(), STARTING_MONEY)))
        every { accountant.getTransactionHistory() } returns transactionsFlow
        coEvery { accountant.finishGame() } returns Unit
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()
        viewModel.onFinishGameClicked()
        advanceUntilIdle()

        transactionsFlow.value = emptyList()
        advanceUntilIdle()

        val state = viewModel.state.value as GameScreenState.GameNotStarted
        assertEquals("", state.startingMoney)
        coVerify { accountant.finishGame() }
    }
}