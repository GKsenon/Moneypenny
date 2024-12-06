package com.gksenon.moneypenny.domain

import javax.inject.Inject
import javax.inject.Named

const val LOCAL_GAME = "local"
const val MULTIPLAYER_HOST_GAME = "host"
const val MULTIPLAYER_CLIENT_GAME = "client"

class AccountantFactory @Inject constructor(
    @Named(LOCAL_GAME) private val localAccountant: Accountant,
    @Named(MULTIPLAYER_HOST_GAME) private val hostAccountant: Accountant,
    @Named(MULTIPLAYER_CLIENT_GAME) private val clientAccountant: Accountant
) {

    fun getAccountant(type: String): Accountant =
        when (type) {
            MULTIPLAYER_HOST_GAME -> hostAccountant
            MULTIPLAYER_CLIENT_GAME -> clientAccountant
            else -> localAccountant
        }
}