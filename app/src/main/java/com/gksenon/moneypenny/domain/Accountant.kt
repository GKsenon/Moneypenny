package com.gksenon.moneypenny.domain

import kotlinx.coroutines.flow.Flow

interface Accountant {

    fun startGame(startingMoney: Int)

    fun getTransactionHistory(): Flow<List<Int>>

    fun add(amount: Int)

    fun subtract(amount: Int)

    fun finishGame()
}