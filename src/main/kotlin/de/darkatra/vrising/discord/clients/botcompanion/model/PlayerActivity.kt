package de.darkatra.vrising.discord.clients.botcompanion.model

import java.time.Instant

data class PlayerActivity(
    val type: Type,
    val playerName: String,
    val occurred: Instant
) {

    enum class Type {
        CONNECTED,
        DISCONNECTED
    }
}
