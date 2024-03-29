package de.darkatra.vrising.discord.serverstatus.model

import dev.kord.common.entity.Snowflake
import kotlinx.datetime.toKotlinInstant
import java.time.Instant

object ServerStatusMonitorTestUtils {

    const val ID = "id"
    val DISCORD_SERVER_ID = Snowflake(Instant.now().toKotlinInstant()).toString()
    val DISCORD_CHANNEL_ID = Snowflake(Instant.now().toKotlinInstant()).toString()
    const val HOST_NAME = "localhost"
    const val QUERY_PORT = 8081

    fun getServerStatusMonitor(status: ServerStatusMonitorStatus): ServerStatusMonitor {
        return ServerStatusMonitor(
            id = ID,
            discordServerId = DISCORD_SERVER_ID,
            discordChannelId = DISCORD_CHANNEL_ID,
            hostname = HOST_NAME,
            queryPort = QUERY_PORT,
            status = status,
            displayServerDescription = true,
            displayPlayerGearLevel = true
        )
    }
}
