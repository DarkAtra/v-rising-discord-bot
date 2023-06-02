package de.darkatra.vrising.discord

import de.darkatra.vrising.discord.serverstatus.ServerStatusMonitor
import de.darkatra.vrising.discord.serverstatus.ServerStatusMonitorStatus
import dev.kord.common.entity.Snowflake
import kotlinx.datetime.toKotlinInstant
import java.time.Instant

object ServerStatusMonitorTestUtils {

    const val ID = "id"
    val DISCORD_SERVER_ID = Snowflake(Instant.now().toKotlinInstant()).toString()
    val DISCORD_CHANNEL_ID = Snowflake(Instant.now().toKotlinInstant()).toString()
    const val HOST_NAME = "localhost"
    const val QUERY_PORT = 8080

    fun getServerStatusMonitor(status: ServerStatusMonitorStatus): ServerStatusMonitor {
        return ServerStatusMonitor(
            id = ID,
            discordServerId = DISCORD_SERVER_ID,
            discordChannelId = DISCORD_CHANNEL_ID,
            hostName = HOST_NAME,
            queryPort = QUERY_PORT,
            status = status,
            displayServerDescription = false
        )
    }
}
