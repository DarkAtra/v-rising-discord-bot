package de.darkatra.vrising.discord.persistence.model

import dev.kord.common.entity.Snowflake
import kotlinx.datetime.toKotlinInstant
import java.time.Instant

object ServerTestUtils {

    const val ID = "id"
    val DISCORD_SERVER_ID = Snowflake(Instant.now().toKotlinInstant()).toString()
    const val HOST_NAME = "darkatra.de"
    const val QUERY_PORT = 8081
    const val API_HOST_NAME = "localhost"
    const val API_PORT = 8082
    const val API_USERNAME = "test"
    const val API_PASSWORD = "1234"

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
            apiHostname = API_HOST_NAME,
            apiPort = API_PORT,
            apiUsername = API_USERNAME,
            apiPassword = API_PASSWORD
        )
    }
}
