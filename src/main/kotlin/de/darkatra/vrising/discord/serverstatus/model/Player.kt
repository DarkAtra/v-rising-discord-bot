package de.darkatra.vrising.discord.serverstatus.model

data class Player(
    val name: String,
    val gearLevel: Int?,
    val clan: String?,
    val killedVBloods: List<String>?
)
