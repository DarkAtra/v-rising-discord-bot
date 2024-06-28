package de.darkatra.vrising.discord.persistence.model.converter

import de.darkatra.vrising.discord.migration.Schema
import de.darkatra.vrising.discord.migration.SchemaEntityConverter
import de.darkatra.vrising.discord.persistence.model.Error
import de.darkatra.vrising.discord.persistence.model.PlayerActivityFeed
import de.darkatra.vrising.discord.persistence.model.PvpKillFeed
import de.darkatra.vrising.discord.persistence.model.Server
import de.darkatra.vrising.discord.persistence.model.ServerTestUtils.DISCORD_SERVER_ID
import de.darkatra.vrising.discord.persistence.model.ServerTestUtils.HOST_NAME
import de.darkatra.vrising.discord.persistence.model.ServerTestUtils.ID
import de.darkatra.vrising.discord.persistence.model.ServerTestUtils.QUERY_PORT
import de.darkatra.vrising.discord.persistence.model.Status
import de.darkatra.vrising.discord.persistence.model.StatusMonitor
import org.assertj.core.api.Assertions.assertThat
import org.dizitart.no2.collection.Document
import org.dizitart.no2.common.mapper.SimpleNitriteMapper
import org.junit.jupiter.api.Test
import java.time.Instant

class EntityConverterTest {

    private val mapper = SimpleNitriteMapper().apply {
        registerEntityConverter(SchemaEntityConverter())
        registerEntityConverter(ErrorEntityConverter())
        registerEntityConverter(PlayerActivityFeedEntityConverter())
        registerEntityConverter(PvpKillFeedEntityConverter())
        registerEntityConverter(ServerEntityConverter())
        registerEntityConverter(StatusMonitorEntityConverter())
    }

    @Test
    fun `should not change Server on persistence roundtrip`() {

        val server = Server(
            id = ID,
            version = Instant.now().toEpochMilli(),
            discordServerId = DISCORD_SERVER_ID,
            hostname = HOST_NAME,
            queryPort = QUERY_PORT,
            apiHostname = "api-hostname",
            apiPort = 8082,
            apiUsername = "api-username",
            apiPassword = "api-password",
            playerActivityFeed = PlayerActivityFeed(
                status = Status.ACTIVE,
                discordChannelId = "player-activity-feed-discord-channel-id",
                lastUpdated = Instant.now(),
                currentFailedAttempts = 1,
                recentErrors = listOf(
                    Error(
                        message = "error-message-1",
                        timestamp = Instant.now()
                    )
                )
            ),
            pvpKillFeed = PvpKillFeed(
                status = Status.INACTIVE,
                discordChannelId = "pvp-kill-feed-discord-channel-id",
                lastUpdated = Instant.now(),
                currentFailedAttempts = 2,
                recentErrors = listOf(
                    Error(
                        message = "error-message-2",
                        timestamp = Instant.now()
                    )
                )
            ),
            statusMonitor = StatusMonitor(
                status = Status.INACTIVE,
                discordChannelId = "status-monitor-discord-channel-id",
                displayServerDescription = true,
                displayPlayerGearLevel = false,
                currentEmbedMessageId = "current-embed-message-id",
                currentFailedAttempts = 1,
                currentFailedApiAttempts = 2,
                recentErrors = listOf(
                    Error(
                        message = "error-message-3",
                        timestamp = Instant.now()
                    )
                )
            )
        )

        val document = mapper.tryConvert(server, Document::class.java)

        assertThat(mapper.tryConvert(document, Server::class.java)).isEqualTo(server)
    }

    @Test
    fun `should not change Schema on persistence roundtrip`() {

        val schema = Schema(
            appVersion = "V1.4.2"
        )

        val document = mapper.tryConvert(schema, Document::class.java)

        assertThat(mapper.tryConvert(document, Schema::class.java)).isEqualTo(schema)
    }
}
