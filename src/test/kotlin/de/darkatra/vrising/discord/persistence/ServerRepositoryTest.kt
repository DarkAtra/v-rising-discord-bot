package de.darkatra.vrising.discord.persistence

import de.darkatra.vrising.discord.DatabaseConfigurationTestUtils
import de.darkatra.vrising.discord.persistence.model.ServerTestUtils
import org.assertj.core.api.Assertions.assertThat
import org.dizitart.no2.Nitrite
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.condition.DisabledInNativeImage

@DisabledInNativeImage
class ServerRepositoryTest {

    private val nitrite: Nitrite = DatabaseConfigurationTestUtils.getTestDatabase()

    private val serverRepository = ServerRepository(nitrite)

    @BeforeEach
    fun setUp() {
        DatabaseConfigurationTestUtils.clearDatabase(nitrite)
    }

    @Test
    fun `should get servers`() {

        serverRepository.addServer(
            ServerTestUtils.getServer()
        )

        val serverStatusMonitors = serverRepository.getServers()

        assertThat(serverStatusMonitors).hasSize(1)

        val serverStatusMonitor = serverStatusMonitors.first()
        assertThat(serverStatusMonitor.id).isEqualTo(ServerTestUtils.ID)
        assertThat(serverStatusMonitor.discordServerId).isEqualTo(ServerTestUtils.DISCORD_SERVER_ID)
        assertThat(serverStatusMonitor.hostname).isEqualTo(ServerTestUtils.HOST_NAME)
        assertThat(serverStatusMonitor.queryPort).isEqualTo(ServerTestUtils.QUERY_PORT)
    }

    @Test
    fun `should not update server status monitor with higher version`() {

        val serverStatusMonitor = ServerTestUtils.getServer()
        serverRepository.addServer(serverStatusMonitor)

        val update1 = serverRepository.getServer(serverStatusMonitor.id, serverStatusMonitor.discordServerId)!!.apply {
            hostname = "test-1"
        }
        val update2 = serverRepository.getServer(serverStatusMonitor.id, serverStatusMonitor.discordServerId)!!.apply {
            hostname = "test-2"
        }

        serverRepository.updateServer(update1)

        val e = assertThrows<OutdatedServerException> {
            serverRepository.updateServer(update2)
        }

        assertThat(e.message).isEqualTo("Server with id '${serverStatusMonitor.id}' was already updated by another thread.")
    }

    @Test
    fun `should not insert server status monitor when using updateServerStatusMonitor`() {

        val e = assertThrows<OutdatedServerException> {
            serverRepository.updateServer(
                ServerTestUtils.getServer()
            )
        }

        assertThat(e.message).isEqualTo("Server with id '${ServerTestUtils.ID}' not found.")
    }
}
