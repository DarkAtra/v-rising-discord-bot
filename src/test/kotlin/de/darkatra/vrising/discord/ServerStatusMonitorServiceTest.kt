package de.darkatra.vrising.discord

import org.assertj.core.api.Assertions.assertThat
import org.dizitart.no2.Nitrite
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledInNativeImage
import org.mockito.Mockito
import java.time.Duration

@DisabledInNativeImage
class ServerStatusMonitorServiceTest {

    private val nitrite: Nitrite = DatabaseConfigurationTestUtils.getTestDatabase()
    private val serverQueryClient: ServerQueryClient = Mockito.mock(ServerQueryClient::class.java)
    private val botProperties: BotProperties = BotProperties().apply {
        updateDelay = Duration.ofSeconds(1)
    }

    private val serverStatusMonitorService = ServerStatusMonitorService(nitrite, serverQueryClient, botProperties)

    @BeforeEach
    fun setUp() {
        DatabaseConfigurationTestUtils.clearDatabase(nitrite)
    }

    @Test
    fun `should get active server status monitors`() {

        serverStatusMonitorService.putServerStatusMonitor(
            ServerStatusMonitorTestUtils.getServerStatusMonitor(ServerStatusMonitorStatus.ACTIVE)
        )

        val serverStatusMonitors = serverStatusMonitorService.getServerStatusMonitors(status = ServerStatusMonitorStatus.ACTIVE)

        assertThat(serverStatusMonitors).hasSize(1)

        val serverStatusMonitor = serverStatusMonitors.first()
        assertThat(serverStatusMonitor.id).isEqualTo(ServerStatusMonitorTestUtils.ID)
        assertThat(serverStatusMonitor.discordServerId).isEqualTo(ServerStatusMonitorTestUtils.DISCORD_SERVER_ID)
        assertThat(serverStatusMonitor.discordChannelId).isEqualTo(ServerStatusMonitorTestUtils.DISCORD_CHANNEL_ID)
        assertThat(serverStatusMonitor.hostName).isEqualTo(ServerStatusMonitorTestUtils.HOST_NAME)
        assertThat(serverStatusMonitor.queryPort).isEqualTo(ServerStatusMonitorTestUtils.QUERY_PORT)
    }

    @Test
    fun `should get no active server status monitors`() {

        serverStatusMonitorService.putServerStatusMonitor(
            ServerStatusMonitorTestUtils.getServerStatusMonitor(ServerStatusMonitorStatus.INACTIVE)
        )

        val serverStatusMonitors = serverStatusMonitorService.getServerStatusMonitors(status = ServerStatusMonitorStatus.ACTIVE)

        assertThat(serverStatusMonitors).hasSize(0)
    }
}
