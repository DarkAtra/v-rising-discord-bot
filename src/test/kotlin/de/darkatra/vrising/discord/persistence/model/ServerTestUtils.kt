package de.darkatra.vrising.discord.persistence.model

import dev.kord.common.entity.Snowflake
import kotlinx.datetime.toKotlinInstant
import java.time.Instant

object ServerTestUtils {

    const val ID = "id"
    val DISCORD_SERVER_ID = Snowflake(Instant.now().toKotlinInstant()).toString()
    const val HOST_NAME = "localhost"
    const val QUERY_PORT = 8081

    fun getServer(): Server {
        return Server(
            id = ID,
            version = Version(
                revision = 1,
                updated = Instant.now()
            ),
            discordServerId = DISCORD_SERVER_ID,
            hostname = HOST_NAME,
            queryPort = QUERY_PORT,
        )
    }
}
