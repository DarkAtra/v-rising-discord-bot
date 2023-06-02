package de.darkatra.vrising.discord.serverstatus.model

data class ServerInfo(
    val name: String,
    val ip: String,
    val gamePort: Int,
    val queryPort: Int,
    val numberOfPlayers: Int,
    val maxPlayers: Int,
    val players: List<Player>,
    val rules: Map<String, Any>,
)
