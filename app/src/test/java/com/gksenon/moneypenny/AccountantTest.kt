package com.gksenon.moneypenny

import com.gksenon.moneypenny.data.InMemoryAccountant
import com.gksenon.moneypenny.domain.Accountant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

private const val STARTING_MONEY = 1500

class AccountantTest {

    private val accountant: Accountant = InMemoryAccountant()

    companion object {

        @JvmStatic
        fun provideAddMoneyParameters() = listOf(
            Arguments.of(1000, listOf(STARTING_MONEY, 1000)),
            Arguments.of(0, listOf(STARTING_MONEY))
        )

        @JvmStatic
        fun provideSubtractMoneyParameters() = listOf(
            Arguments.of(1000, listOf(STARTING_MONEY, -1000)),
            Arguments.of(0, listOf(STARTING_MONEY))
        )
    }

    @Test
    fun accountant_beforeGameIsStarted_transactionHistoryIsEmpty() = runTest {
        assertTrue(accountant.getTransactionHistory().first().isEmpty())
    }

    @Test
    fun accountant_startGame_shouldInitTransactions() = runTest {
        accountant.startGame(STARTING_MONEY)

        assertEquals(listOf(1500), accountant.getTransactionHistory().first())
    }

    @Test
    fun accountant_getTransactionHistory_shouldReturnTransactions() = runTest {
        accountant.startGame(STARTING_MONEY)
        accountant.add(1000)
        accountant.subtract(500)

        assertEquals(listOf(STARTING_MONEY, 1000, -500), accountant.getTransactionHistory().first())
    }

    @ParameterizedTest
    @MethodSource("provideAddMoneyParameters")
    fun accountant_add_shouldAddCorrectAmount(amount: Int, transactions: List<Int>) = runTest {
        accountant.startGame(STARTING_MONEY)
        accountant.add(amount)

        assertEquals(transactions, accountant.getTransactionHistory().first())
    }

    @ParameterizedTest
    @MethodSource("provideSubtractMoneyParameters")
    fun accountant_subtract_shouldSubtractCorrectAmount(amount: Int, transactions: List<Int>) = runTest {
        accountant.startGame(STARTING_MONEY)
        accountant.subtract(amount)

        assertEquals(transactions, accountant.getTransactionHistory().first())
    }

    @Test
    fun accountant_finishGame_shouldClearBalance() = runTest {
        accountant.startGame(STARTING_MONEY)
        assertEquals(listOf(STARTING_MONEY), accountant.getTransactionHistory().first())

        accountant.finishGame()
        assertTrue(accountant.getTransactionHistory().first().isEmpty())
    }
}