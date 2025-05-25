package de.darkatra.vrising.discord.clients.botcompanion.model

import java.time.Instant

data class Raid(
    val attackers: List<Player>,
    val defenders: List<Player>,
    val occurred: Instant
) {

    data class Player(
        val name: String,
        val gearLevel: Int,
        val clan: String?
    )
}
