package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.persistence.ServerRepository
import de.darkatra.vrising.discord.persistence.model.Server
import de.darkatra.vrising.discord.persistence.model.ServerTestUtils.API_HOST_NAME
import de.darkatra.vrising.discord.persistence.model.ServerTestUtils.API_PASSWORD
import de.darkatra.vrising.discord.persistence.model.ServerTestUtils.API_PORT
import de.darkatra.vrising.discord.persistence.model.ServerTestUtils.API_USERNAME
import de.darkatra.vrising.discord.persistence.model.ServerTestUtils.DISCORD_SERVER_ID
import de.darkatra.vrising.discord.persistence.model.ServerTestUtils.HOST_NAME
import de.darkatra.vrising.discord.persistence.model.ServerTestUtils.QUERY_PORT
import de.darkatra.vrising.discord.persistence.model.Version
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledInNativeImage
import org.mockito.Mockito
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.time.Instant
import java.time.temporal.ChronoUnit

@DisabledInNativeImage
class ServerServiceTest {

    private val serverRepository = Mockito.mock(ServerRepository::class.java)
    private val statusMonitorService = Mockito.mock(StatusMonitorService::class.java)
    private val playerActivityFeedService = Mockito.mock(PlayerActivityFeedService::class.java)
    private val pvpKillFeedService = Mockito.mock(PvpKillFeedService::class.java)

    private val serverService = ServerService(serverRepository, statusMonitorService, playerActivityFeedService, pvpKillFeedService)

    @Test
    fun shouldOffboardInactiveServers() {

        val servers = listOf(
            // should not be offboarded
            Server(
                id = "do-not-offboard",
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
            ),
            // should be offboarded
            Server(
                id = "offboard-me-pls",
                version = Version(
                    revision = 1,
                    updated = Instant.now().minus(8, ChronoUnit.DAYS)
                ),
                discordServerId = DISCORD_SERVER_ID,
                hostname = HOST_NAME,
                queryPort = QUERY_PORT,
                apiHostname = API_HOST_NAME,
                apiPort = API_PORT,
                apiUsername = API_USERNAME,
                apiPassword = API_PASSWORD
            )
        )
        given(serverRepository.getServers()).willReturn(servers)

        serverService.cleanupInactiveServers()

        verify(serverRepository).removeServer("offboard-me-pls")
        verify(serverRepository, never()).removeServer("do-not-offboard")
    }
}
