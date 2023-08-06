package de.darkatra.vrising.discord.clients.botcompanion.model

import java.time.Instant

data class PvpKill(
    val killer: Player,
    val victim: Player,
    val occurred: Instant
) {

    data class Player(
        val name: String,
        val gearLevel: Int
    )
}
