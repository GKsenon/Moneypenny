package com.gksenon.moneypenny

import com.gksenon.moneypenny.domain.Accountant
import com.gksenon.moneypenny.domain.Player
import com.gksenon.moneypenny.domain.Transaction
import com.gksenon.moneypenny.viewmodel.GameViewModel
import com.gksenon.moneypenny.viewmodel.MoneyTransferDialogState
import com.gksenon.moneypenny.viewmodel.PlayerCard
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.UUID

@ExperimentalCoroutinesApi
class GameViewModelTest {

    private val players = listOf(
        Player(UUID.randomUUID(), "Bank", Int.MAX_VALUE),
        Player(UUID.randomUUID(), "First", 1500),
        Player(UUID.randomUUID(), "Second", 1500)
    )
    private val playersFlow = MutableStateFlow(players)
    private val lastTransaction = Transaction(
        id = UUID.randomUUID(),
        time = Instant.now(),
        amount = 200,
        sender = players[1],
        recipient = players[2]
    )
    private val lastTransactionFlow = MutableStateFlow<Transaction?>(lastTransaction)
    private val accountant: Accountant = mockk {
        every { getPlayers() } returns playersFlow
        every { getLastTransaction() } returns lastTransactionFlow
    }

    companion object {

        @JvmStatic
        fun provideAmountValues() = listOf(
            Arguments.of("123", "123", true),
            Arguments.of("", "", false),
            Arguments.of("1e23.-,r4", "1234", true),
            Arguments.of("123456789000", "123456789", true),
            Arguments.of("-123", "123", true),
            Arguments.of("0", "0", false)
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
    fun init_createsPlayerCardsAndLastTransactionCard() = runTest {
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()

        val state = viewModel.state.value
        state.playerCards.forEachIndexed { index, card -> validatePlayerCard(players[index], card) }
        assertTrue(state.moneyTransferDialogState is MoneyTransferDialogState.Closed)
        assertEquals(lastTransaction, state.lastTransaction)
    }

    @Test
    fun onPlayerChanged_updatesPlayer() = runTest {
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()

        val initState = viewModel.state.value
        assertEquals(players.last().balance, initState.playerCards.last().player.balance)

        playersFlow.value = listOf(players[0], players[1], players[2].copy(balance = 2000))
        advanceUntilIdle()

        val updatedState = viewModel.state.value
        assertEquals(2000, updatedState.playerCards.last().player.balance)
        assertEquals(initState.playerCards.last().color, updatedState.playerCards.last().color)
    }

    @Test
    fun onPlayerClicked_showsTransferDialog() = runTest {
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()

        val sender = players.first()
        viewModel.onPlayerClicked(sender)
        advanceUntilIdle()

        val dialogState =
            viewModel.state.value.moneyTransferDialogState as MoneyTransferDialogState.Opened
        assertEquals(sender, dialogState.sender)
        assertEquals(players.minus(sender), dialogState.availableRecipients)
        assertNotEquals(sender, dialogState.selectedRecipient)
        assertEquals(dialogState.amount, "")
        assertFalse(dialogState.isConfirmButtonEnabled)
    }

    @Test
    fun onRecipientChanged_changesRecipient() = runTest {
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()

        viewModel.onPlayerClicked(players.first())
        advanceUntilIdle()

        viewModel.onRecipientChanged(players.last().id)
        advanceUntilIdle()

        val dialogState =
            viewModel.state.value.moneyTransferDialogState as MoneyTransferDialogState.Opened
        assertEquals(players.last(), dialogState.selectedRecipient)
    }

    @ParameterizedTest
    @MethodSource("provideAmountValues")
    fun onAmountChanged_validatesAmount(
        input: String,
        expectedOutput: String,
        isConfirmButtonEnabled: Boolean
    ) = runTest {
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()

        viewModel.onPlayerClicked(players.first())
        advanceUntilIdle()

        viewModel.onAmountChanged(input)
        advanceUntilIdle()

        val dialogState =
            viewModel.state.value.moneyTransferDialogState as MoneyTransferDialogState.Opened
        assertEquals(expectedOutput, dialogState.amount)
        assertEquals(isConfirmButtonEnabled, dialogState.isConfirmButtonEnabled)
    }

    @Test
    fun onMoneyTransferDialogConfirmed_sendsMoney() = runTest {
        val senderIdSlot = slot<UUID>()
        val recipientIdSlot = slot<UUID>()
        val amountSlot = slot<Int>()
        coEvery {
            accountant.sendMoney(
                senderId = capture(senderIdSlot),
                recipientId = capture(recipientIdSlot),
                amount = capture(amountSlot)
            )
        } returns Unit
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()

        val sender = players.first()
        viewModel.onPlayerClicked(sender)
        advanceUntilIdle()

        val recipient = players.last()
        viewModel.onRecipientChanged(recipient.id)
        advanceUntilIdle()

        val amount = "200"
        viewModel.onAmountChanged(amount)
        advanceUntilIdle()

        viewModel.onMoneyTransferDialogConfirmed()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(MoneyTransferDialogState.Closed, state.moneyTransferDialogState)
        assertEquals(sender.id, senderIdSlot.captured)
        assertEquals(recipient.id, recipientIdSlot.captured)
        assertEquals(200, amountSlot.captured)
    }

    @Test
    fun onLastTransactionChanged_updatesTransaction() = runTest {
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()

        val newLastTransaction = Transaction(
            id = UUID.randomUUID(),
            time = Instant.now(),
            amount = 200,
            sender = players[1],
            recipient = players[2]
        )
        lastTransactionFlow.value = newLastTransaction
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(newLastTransaction, state.lastTransaction)
    }

    @Test
    fun onCancelLastTransactionButtonClicked_showConfirmationDialog() = runTest {
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()

        viewModel.onCancelTransactionButtonClicked()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.showCancelLastTransactionConfirmation)
    }

    @Test
    fun onCancelLastTransactionConfirmationDialogConfirmed_cancelsTransaction() = runTest {
        val transactionIdSlot = slot<UUID>()
        coEvery { accountant.cancelTransaction(capture(transactionIdSlot)) } returns Unit
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()

        viewModel.onCancelTransactionButtonClicked()
        advanceUntilIdle()

        viewModel.onCancelLastTransactionConfirmationDialogConfirmed()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.showCancelLastTransactionConfirmation)
        assertEquals(lastTransaction.id, transactionIdSlot.captured)
    }

    @Test
    fun onCancelLastTransactionConfirmationDialogDismissed_closesDialog() = runTest {
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()

        viewModel.onCancelTransactionButtonClicked()
        advanceUntilIdle()

        viewModel.onCancelLastTransactionConfirmationDialogDismissed()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.showCancelLastTransactionConfirmation)
    }

    @Test
    fun onMoneyTransferDialogDismissed_closesDialog() = runTest {
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()

        viewModel.onPlayerClicked(players.first())
        advanceUntilIdle()

        viewModel.onMoneyTransferDialogDismissed()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(MoneyTransferDialogState.Closed, state.moneyTransferDialogState)
    }

    @Test
    fun onFinishButtonClicked_showsConfirmationDialog() = runTest {
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()

        viewModel.onFinishButtonClicked()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.showFinishConfirmation)
    }

    @Test
    fun onFinishConfirmationDialogConfirmed_finishesGame() = runTest {
        coEvery { accountant.finishGame() } returns Unit
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()

        viewModel.onFinishButtonClicked()
        advanceUntilIdle()

        viewModel.onFinishConfirmationDialogConfirmed()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.showFinishConfirmation)
        coVerify { accountant.finishGame() }
    }

    @Test
    fun onFinishConfirmationDialogDismissed_closesDialog() = runTest {
        val viewModel = GameViewModel(accountant)
        advanceUntilIdle()

        viewModel.onFinishButtonClicked()
        advanceUntilIdle()

        viewModel.onFinishConfirmationDialogDismissed()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.showFinishConfirmation)
    }

    private fun validatePlayerCard(player: Player, card: PlayerCard) {
        assertEquals(player, card.player)
        assertTrue(card.color.toList().all { color -> color in 0..256 })
    }
}