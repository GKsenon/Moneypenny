package com.gksenon.moneypenny.data

import kotlinx.serialization.Serializable

@Serializable
sealed class Message {

    @Serializable
    class Accepted(val id: String): Message()

    @Serializable
    class Start(val startingMoney: Int, val players: List<PlayerEntity>) : Message()

    @Serializable
    class SaveTransaction(
        val id: String,
        val time: Long,
        val senderId: String,
        val recipientId: String,
        val amount: Int
    ) : Message()

    @Serializable
    class DeleteTransaction() : Message()

    @Serializable
    class Finish() : Message()
}

@Serializable
data class PlayerEntity(val id: String, val name: String)
