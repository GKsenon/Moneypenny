package com.gksenon.moneypenny.data

import kotlinx.serialization.Serializable

@Serializable
sealed class Message {
    @Serializable
    class Start(val startingMoney: Int, val players: List<PlayerEntity>): Message()
}

@Serializable
data class PlayerEntity(val id: String, val name: String)
