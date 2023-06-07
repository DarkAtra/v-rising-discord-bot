package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.DatabaseConfigurationTestUtils
import de.darkatra.vrising.discord.ServerStatusMonitorTestUtils
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitorStatus
import org.assertj.core.api.Assertions.assertThat
import org.dizitart.no2.Nitrite
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledInNativeImage

@DisabledInNativeImage
class ServerStatusMonitorRepositoryTest {

    private val nitrite: Nitrite = DatabaseConfigurationTestUtils.getTestDatabase()

    private val serverStatusMonitorRepository = ServerStatusMonitorRepository(nitrite)

    @BeforeEach
    fun setUp() {
        DatabaseConfigurationTestUtils.clearDatabase(nitrite)
    }

    @Test
    fun `should get active server status monitors`() {

        serverStatusMonitorRepository.putServerStatusMonitor(
            ServerStatusMonitorTestUtils.getServerStatusMonitor(ServerStatusMonitorStatus.ACTIVE)
        )

        val serverStatusMonitors = serverStatusMonitorRepository.getServerStatusMonitors(status = ServerStatusMonitorStatus.ACTIVE)

        assertThat(serverStatusMonitors).hasSize(1)

        val serverStatusMonitor = serverStatusMonitors.first()
        assertThat(serverStatusMonitor.id).isEqualTo(ServerStatusMonitorTestUtils.ID)
        assertThat(serverStatusMonitor.discordServerId).isEqualTo(ServerStatusMonitorTestUtils.DISCORD_SERVER_ID)
        assertThat(serverStatusMonitor.discordChannelId).isEqualTo(ServerStatusMonitorTestUtils.DISCORD_CHANNEL_ID)
        assertThat(serverStatusMonitor.hostname).isEqualTo(ServerStatusMonitorTestUtils.HOST_NAME)
        assertThat(serverStatusMonitor.queryPort).isEqualTo(ServerStatusMonitorTestUtils.QUERY_PORT)
    }

    @Test
    fun `should get no active server status monitors`() {

        serverStatusMonitorRepository.putServerStatusMonitor(
            ServerStatusMonitorTestUtils.getServerStatusMonitor(ServerStatusMonitorStatus.INACTIVE)
        )

        val serverStatusMonitors = serverStatusMonitorRepository.getServerStatusMonitors(status = ServerStatusMonitorStatus.ACTIVE)

        assertThat(serverStatusMonitors).hasSize(0)
    }
}
