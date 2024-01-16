package com.gksenon.moneypenny

import com.gksenon.moneypenny.data.InMemoryAccountant
import com.gksenon.moneypenny.domain.Accountant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountantTest {

    private val accountant: Accountant = InMemoryAccountant()

    @Test
    fun accountant_beforeGameIsStarted_balanceIsNull() = runTest {
        advanceUntilIdle()
        assertNull(accountant.getBalance().first())
    }

    @Test
    fun accountant_startGame_shouldInitBalance() = runTest {
        accountant.startGame(1500)
        advanceUntilIdle()

        assertEquals(1500, accountant.getBalance().first())
    }

    @Test
    fun accountant_add_shouldAddCorrectAmount() = runTest {
        accountant.startGame(1500)
        advanceUntilIdle()

        accountant.add(1000)
        advanceUntilIdle()

        assertEquals(2500, accountant.getBalance().first())
    }

    @Test
    fun accountant_subtract_shouldSubtractCorrectAmount() = runTest {
        accountant.startGame(1500)
        advanceUntilIdle()

        accountant.subtract(1000)
        advanceUntilIdle()

        assertEquals(500, accountant.getBalance().first())
    }

    @Test
    fun accountant_finishGame_shouldClearBalance() = runTest {
        accountant.startGame(1500)
        advanceUntilIdle()
        assertEquals(1500, accountant.getBalance().first())

        accountant.finishGame()
        advanceUntilIdle()
        assertNull(accountant.getBalance().first())
    }
}