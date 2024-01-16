package com.gksenon.moneypenny.domain

import kotlinx.coroutines.flow.Flow

interface Accountant {

    fun startGame(startingMoney: Int)

    fun getBalance(): Flow<Int?>

    fun add(amount: Int)

    fun subtract(amount: Int)

    fun finishGame()
}