package de.darkatra.vrising.discord.serverstatus.model

import de.darkatra.vrising.discord.clients.botcompanion.model.Character
import de.darkatra.vrising.discord.clients.botcompanion.model.VBlood
import de.darkatra.vrising.discord.clients.serverquery.model.ServerStatus

data class ServerInfo(
    val name: String,
    val ip: String,
    val gamePort: Int,
    val queryPort: Int,
    val numberOfPlayers: Int,
    val maxPlayers: Int,
    val players: List<Player>,
    val rules: Map<String, Any>,
) {

    fun enrichCompanionData(characters: List<Character>) {

        players.forEach { player ->
            val character = characters.find { character -> character.name == player.name }
            player.gearLevel = character?.gearLevel
            player.clan = character?.clan
            player.killedVBloods = character?.killedVBloods?.map(VBlood::displayName)
        }
    }

    companion object {

        fun of(serverStatus: ServerStatus): ServerInfo {

            return ServerInfo(
                name = serverStatus.serverInfo.name,
                ip = serverStatus.serverInfo.hostAddress,
                gamePort = serverStatus.serverInfo.gamePort,
                queryPort = serverStatus.serverInfo.port,
                numberOfPlayers = serverStatus.serverInfo.numOfPlayers,
                maxPlayers = serverStatus.serverInfo.maxPlayers,
                players = serverStatus.players.map { sourcePlayer ->
                    Player(
                        name = sourcePlayer.name
                    )
                },
                rules = serverStatus.rules
            )
        }
    }
}
