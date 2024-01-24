package com.gksenon.moneypenny.data

import com.gksenon.moneypenny.domain.Accountant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryAccountant : Accountant {

    private val transactions = MutableStateFlow<List<Int>>(emptyList())

    override fun startGame(startingMoney: Int) {
        transactions.value = listOf(startingMoney)
    }

    override fun getTransactionHistory(): Flow<List<Int>> = transactions

    override fun add(amount: Int) {
        if (amount != 0)
            transactions.value = transactions.value.plus(amount)
    }

    override fun subtract(amount: Int) {
        if (amount != 0)
            transactions.value = transactions.value.plus(-amount)
    }

    override fun finishGame() {
        transactions.value = emptyList()
    }
}