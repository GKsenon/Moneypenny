package com.gksenon.moneypenny.data

import com.gksenon.moneypenny.domain.Accountant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryAccountant: Accountant {

    private val balance = MutableStateFlow<Int?>(null)

    override fun startGame(startingMoney: Int) {
        balance.value = startingMoney
    }

    override fun getBalance(): Flow<Int?> = balance

    override fun add(amount: Int) {
        balance.value = balance.value!! + amount
    }

    override fun subtract(amount: Int) {
        balance.value = balance.value!! - amount
    }

    override fun finishGame() {
        balance.value = null
    }
}