package de.darkatra.vrising.discord.clients.botcompanion.model

import java.time.Instant

data class VBloodKill(
    val killers: List<Player>,
    val vBlood: VBlood,
    val occurred: Instant
) {

    data class Player(
        val name: String,
        val gearLevel: Int
    )
}
