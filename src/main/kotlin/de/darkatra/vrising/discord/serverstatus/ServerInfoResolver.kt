package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.clients.botcompanion.BotCompanionClient
import de.darkatra.vrising.discord.clients.botcompanion.model.PlayerActivity
import de.darkatra.vrising.discord.clients.botcompanion.model.PvpKill
import de.darkatra.vrising.discord.clients.botcompanion.model.VBlood
import de.darkatra.vrising.discord.clients.serverquery.ServerQueryClient
import de.darkatra.vrising.discord.serverstatus.model.Player
import de.darkatra.vrising.discord.serverstatus.model.ServerInfo
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.support.BasicAuthenticationInterceptor
import org.springframework.stereotype.Service

@Service
class ServerInfoResolver(
    private val serverQueryClient: ServerQueryClient,
    private val botCompanionClient: BotCompanionClient,
) {

    suspend fun getServerInfo(serverStatusMonitor: ServerStatusMonitor): ServerInfo {

        val serverStatus = serverQueryClient.getServerStatus(serverStatusMonitor.hostname, serverStatusMonitor.queryPort)
        val characters = when {
            serverStatusMonitor.apiEnabled -> botCompanionClient.getCharacters(
                serverStatusMonitor.apiHostname!!,
                serverStatusMonitor.apiPort!!,
                getInterceptors(serverStatusMonitor)
            )

            else -> emptyList()
        }

        return ServerInfo(
            name = serverStatus.serverInfo.name,
            ip = serverStatus.serverInfo.hostAddress,
            gamePort = serverStatus.serverInfo.gamePort,
            queryPort = serverStatus.serverInfo.port,
            numberOfPlayers = serverStatus.serverInfo.numOfPlayers,
            maxPlayers = serverStatus.serverInfo.maxPlayers,
            players = serverStatus.players.map { sourcePlayer ->
                val character = characters.find { character -> character.name == sourcePlayer.name }
                Player(
                    name = sourcePlayer.name,
                    gearLevel = character?.gearLevel,
                    clan = character?.clan,
                    killedVBloods = character?.killedVBloods?.map(VBlood::displayName)
                )
            },
            rules = serverStatus.rules
        )
    }

    suspend fun getPlayerActivities(serverStatusMonitor: ServerStatusMonitor): List<PlayerActivity> {

        return when {
            serverStatusMonitor.apiEnabled -> botCompanionClient.getPlayerActivities(
                serverStatusMonitor.apiHostname!!,
                serverStatusMonitor.apiPort!!,
                getInterceptors(serverStatusMonitor)
            )

            else -> emptyList()
        }
    }

    suspend fun getPvpKills(serverStatusMonitor: ServerStatusMonitor): List<PvpKill> {

        return when {
            serverStatusMonitor.apiEnabled -> botCompanionClient.getPvpKills(
                serverStatusMonitor.apiHostname!!,
                serverStatusMonitor.apiPort!!,
                getInterceptors(serverStatusMonitor)
            )

            else -> emptyList()
        }
    }

    private fun getInterceptors(serverStatusMonitor: ServerStatusMonitor): List<ClientHttpRequestInterceptor> {

        val (_, _, _, _, _, _, _, _, _, _, apiUsername, apiPassword, _, _, _, _, _, _) = serverStatusMonitor

        return when (apiUsername != null && apiPassword != null) {
            true -> listOf(BasicAuthenticationInterceptor(apiUsername, apiPassword))
            false -> emptyList()
        }
    }
}
