package de.darkatra.vrising.discord.serverstatus.model

object ServerInfoTestUtils {

    const val NAME = "server-name"
    const val IP = "localhost"
    const val GAME_PORT = 8080
    const val DESCRIPTION = "Server description"
    const val NUMBER_OF_PLAYERS = 1
    const val MAX_PLAYERS = 10
    const val DAYS_RUNNING = 7

    fun getServerInfo(): ServerInfo {
        return ServerInfo(
            name = NAME,
            ip = IP,
            gamePort = GAME_PORT,
            queryPort = 8081,
            numberOfPlayers = NUMBER_OF_PLAYERS,
            maxPlayers = MAX_PLAYERS,
            players = listOf(
                Player(
                    name = "Atra",
                    gearLevel = null,
                    clan = null,
                    killedVBloods = null
                )
            ),
            rules = mapOf(
                "desc0" to DESCRIPTION,
                "days-runningv2" to DAYS_RUNNING
            )
        )
    }
}
