package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.DatabaseConfigurationTestUtils
import de.darkatra.vrising.discord.persistence.ServerStatusMonitorRepository
import de.darkatra.vrising.discord.persistence.model.ServerStatusMonitorStatus
import de.darkatra.vrising.discord.serverstatus.exceptions.OutdatedServerStatusMonitorException
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitorTestUtils
import org.assertj.core.api.Assertions.assertThat
import org.dizitart.no2.Nitrite
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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

        serverStatusMonitorRepository.addServerStatusMonitor(
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

        serverStatusMonitorRepository.addServerStatusMonitor(
            ServerStatusMonitorTestUtils.getServerStatusMonitor(ServerStatusMonitorStatus.INACTIVE)
        )

        val serverStatusMonitors = serverStatusMonitorRepository.getServerStatusMonitors(status = ServerStatusMonitorStatus.ACTIVE)

        assertThat(serverStatusMonitors).hasSize(0)
    }

    @Test
    fun `should not update server status monitor with higher version`() {

        val serverStatusMonitor = ServerStatusMonitorTestUtils.getServerStatusMonitor(ServerStatusMonitorStatus.ACTIVE)
        serverStatusMonitorRepository.addServerStatusMonitor(serverStatusMonitor)

        val update1 = serverStatusMonitorRepository.getServerStatusMonitor(serverStatusMonitor.id, serverStatusMonitor.discordServerId)!!.apply {
            status = ServerStatusMonitorStatus.INACTIVE
        }
        val update2 = serverStatusMonitorRepository.getServerStatusMonitor(serverStatusMonitor.id, serverStatusMonitor.discordServerId)!!.apply {
            status = ServerStatusMonitorStatus.ACTIVE
        }

        serverStatusMonitorRepository.updateServerStatusMonitor(update1)

        val e = assertThrows<OutdatedServerStatusMonitorException> {
            serverStatusMonitorRepository.updateServerStatusMonitor(update2)
        }

        assertThat(e.message).isEqualTo("Monitor with id '${serverStatusMonitor.id}' was already updated by another thread.")
    }

    @Test
    fun `should not insert server status monitor when using updateServerStatusMonitor`() {

        val e = assertThrows<OutdatedServerStatusMonitorException> {
            serverStatusMonitorRepository.updateServerStatusMonitor(
                ServerStatusMonitorTestUtils.getServerStatusMonitor(ServerStatusMonitorStatus.ACTIVE)
            )
        }

        assertThat(e.message).isEqualTo("Monitor with id '${ServerStatusMonitorTestUtils.ID}' not found.")
    }
}
