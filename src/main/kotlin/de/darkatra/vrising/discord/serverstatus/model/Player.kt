package de.darkatra.vrising.discord.serverstatus.model

data class Player(
    val name: String,
    var gearLevel: Int? = null,
    var clan: String? = null,
    var killedVBloods: List<String>? = null
)
