package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.clients.botcompanion.BotCompanionClient
import de.darkatra.vrising.discord.clients.botcompanion.model.VBlood
import de.darkatra.vrising.discord.clients.serverquery.ServerQueryClient
import de.darkatra.vrising.discord.serverstatus.model.Player
import de.darkatra.vrising.discord.serverstatus.model.ServerInfo
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor
import org.springframework.stereotype.Service

@Service
class ServerInfoResolver(
    private val serverQueryClient: ServerQueryClient,
    private val botCompanionClient: BotCompanionClient,
) {

    suspend fun getServerInfo(serverStatusMonitor: ServerStatusMonitor): ServerInfo {

        val serverInfo = serverQueryClient.getServerInfo(serverStatusMonitor.hostname, serverStatusMonitor.queryPort)
        val players = serverQueryClient.getPlayerList(serverStatusMonitor.hostname, serverStatusMonitor.queryPort)
        val rules = serverQueryClient.getRules(serverStatusMonitor.hostname, serverStatusMonitor.queryPort)
        val characters = when {
            serverStatusMonitor.apiEnabled -> botCompanionClient.getCharacters(serverStatusMonitor.apiHostname!!, serverStatusMonitor.apiPort!!)
            else -> emptyList()
        }

        return ServerInfo(
            name = serverInfo.name,
            ip = serverInfo.hostAddress,
            gamePort = serverInfo.gamePort,
            queryPort = serverInfo.port,
            numberOfPlayers = serverInfo.numOfPlayers,
            maxPlayers = serverInfo.maxPlayers,
            players = players.map { sourcePlayer ->
                val character = characters.find { character -> character.name == sourcePlayer.name }
                Player(
                    name = sourcePlayer.name,
                    gearLevel = character?.gearLevel,
                    clan = character?.clan,
                    killedVBloods = character?.killedVBloods?.map(VBlood::displayName)
                )
            },
            rules = rules
        )
    }
}
