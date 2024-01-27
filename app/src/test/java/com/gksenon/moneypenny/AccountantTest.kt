package com.gksenon.moneypenny

import com.gksenon.moneypenny.domain.Accountant
import com.gksenon.moneypenny.domain.AccountantGateway
import com.gksenon.moneypenny.domain.Transaction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.joda.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.UUID

private const val STARTING_MONEY = 1500

class AccountantTest {

    private val gateway: AccountantGateway = mockk()
    private val accountant: Accountant = Accountant(gateway)

    companion object {

        @JvmStatic
        fun provideAddMoneyParameters() = listOf(
            Arguments.of(1000, true),
            Arguments.of(0, false)
        )

        @JvmStatic
        fun provideSubtractMoneyParameters() = listOf(
            Arguments.of(1000, true),
            Arguments.of(0, false)
        )
    }

    @Test
    fun accountant_beforeGameIsStarted_transactionHistoryIsEmpty() = runTest {
        every { gateway.readTransactions() } returns flowOf(emptyList())
        assertTrue(accountant.getTransactionHistory().first().isEmpty())
    }

    @Test
    fun accountant_startGame_shouldInitTransactions() = runTest {
        val transactionSlot = slot<Transaction>()
        coEvery { gateway.writeTransaction(capture(transactionSlot)) } returns Unit
        accountant.startGame(STARTING_MONEY)

        assertEquals(STARTING_MONEY, transactionSlot.captured.amount)
    }

    @Test
    fun accountant_getTransactionHistory_shouldReturnSortedTransactions() = runTest {
        val transactions = listOf(
            Transaction(id = UUID.randomUUID(), time = Instant.now(), amount = 1000),
            Transaction(id = UUID.randomUUID(), time = Instant.now().minus(120), amount = 3000),
            Transaction(id = UUID.randomUUID(), time = Instant.now().minus(60), amount = -2500)
        )
        every { gateway.readTransactions() } returns flowOf(transactions)

        assertEquals(transactions, accountant.getTransactionHistory().first())
    }

    @ParameterizedTest
    @MethodSource("provideAddMoneyParameters")
    fun accountant_add_shouldAddCorrectAmount(amount: Int, shouldSaveTransaction: Boolean) = runTest {
        val transactionSlot = slot<Transaction>()
        coEvery { gateway.writeTransaction(capture(transactionSlot)) } returns Unit
        accountant.add(amount)

        assertEquals(shouldSaveTransaction, transactionSlot.isCaptured)
        if(shouldSaveTransaction)
            assertEquals(amount, transactionSlot.captured.amount)
    }

    @ParameterizedTest
    @MethodSource("provideSubtractMoneyParameters")
    fun accountant_subtract_shouldSubtractCorrectAmount(amount: Int, shouldSaveTransaction: Boolean) = runTest {
        val transactionSlot = slot<Transaction>()
        coEvery { gateway.writeTransaction(capture(transactionSlot)) } returns Unit
        accountant.subtract(amount)

        assertEquals(shouldSaveTransaction, transactionSlot.isCaptured)
        if(shouldSaveTransaction)
            assertEquals(-amount, transactionSlot.captured.amount)
    }

    @Test
    fun accountant_finishGame_shouldClearTransactions() = runTest {
        coEvery { gateway.clear() } returns Unit
        accountant.finishGame()
        coVerify { gateway.clear() }
    }
}