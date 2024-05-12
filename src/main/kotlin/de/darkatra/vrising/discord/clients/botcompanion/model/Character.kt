package de.darkatra.vrising.discord.clients.botcompanion.model

data class Character(
    val name: String,
    val gearLevel: Int,
    val clan: String?,
    val killedVBloods: List<VBlood>
)
