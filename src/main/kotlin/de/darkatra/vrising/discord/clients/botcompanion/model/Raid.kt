package de.darkatra.vrising.discord.clients.botcompanion.model

import java.time.Instant

data class Raid(
    val attackers: List<Player>,
    val defenders: List<Player>,
    val occurred: Instant,
    val updated: Instant? = null // nullable since it was introduced in bot-companion v0.9.0
) {

    data class Player(
        val name: String,
        val gearLevel: Int,
        val clan: String?,
        val joinedAt: Instant? = null // nullable since it was introduced in bot-companion v0.9.0
    )
}
