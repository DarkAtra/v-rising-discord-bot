package de.darkatra.vrising.discord.persistence

import ch.qos.logback.classic.Logger
import de.darkatra.vrising.discord.DatabaseConfigurationTestUtils
import de.darkatra.vrising.discord.persistence.model.ServerTestUtils
import org.assertj.core.api.Assertions.assertThat
import org.dizitart.no2.Nitrite
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.condition.DisabledInNativeImage
import org.slf4j.LoggerFactory

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

        val servers = serverRepository.getServers()

        assertThat(servers).hasSize(1)

        val server = servers.first()
        assertThat(server.id).isEqualTo(ServerTestUtils.ID)
        assertThat(server.discordServerId).isEqualTo(ServerTestUtils.DISCORD_SERVER_ID)
        assertThat(server.hostname).isEqualTo(ServerTestUtils.HOST_NAME)
        assertThat(server.queryPort).isEqualTo(ServerTestUtils.QUERY_PORT)
    }

    @Test
    fun `should get server`() {

        serverRepository.addServer(
            ServerTestUtils.getServer()
        )

        val server = serverRepository.getServer(ServerTestUtils.ID)

        assertThat(server).isNotNull()
        assertThat(server!!.id).isEqualTo(ServerTestUtils.ID)
        assertThat(server.discordServerId).isEqualTo(ServerTestUtils.DISCORD_SERVER_ID)
        assertThat(server.hostname).isEqualTo(ServerTestUtils.HOST_NAME)
        assertThat(server.queryPort).isEqualTo(ServerTestUtils.QUERY_PORT)
    }

    @Test
    fun `should get server with discordServerId`() {

        serverRepository.addServer(
            ServerTestUtils.getServer()
        )

        val server = serverRepository.getServer(ServerTestUtils.ID, ServerTestUtils.DISCORD_SERVER_ID)

        assertThat(server).isNotNull()
        assertThat(server!!.id).isEqualTo(ServerTestUtils.ID)
        assertThat(server.discordServerId).isEqualTo(ServerTestUtils.DISCORD_SERVER_ID)
        assertThat(server.hostname).isEqualTo(ServerTestUtils.HOST_NAME)
        assertThat(server.queryPort).isEqualTo(ServerTestUtils.QUERY_PORT)
    }

    @Test
    fun `should not get server with non matching discordServerId`() {

        serverRepository.addServer(
            ServerTestUtils.getServer()
        )

        val server = serverRepository.getServer(ServerTestUtils.ID, "invalid-discord-server-id")

        assertThat(server).isNull()
    }

    @Test
    fun `should not update server status monitor with higher version`() {

        val logger = LoggerFactory.getLogger("nitrite") as Logger
        logger.level = ch.qos.logback.classic.Level.DEBUG

        val server = ServerTestUtils.getServer()
        serverRepository.addServer(server)

        val update1 = serverRepository.getServer(server.id, server.discordServerId)!!.apply {
            hostname = "test-1"
        }
        val update2 = serverRepository.getServer(server.id, server.discordServerId)!!.apply {
            hostname = "test-2"
        }

        println("Update 1")
        serverRepository.updateServer(update1)

        println("Update 2")
        val e = assertThrows<OutdatedServerException> {
            serverRepository.updateServer(update2)
        }

        assertThat(e.message).isEqualTo("Server with id '${server.id}' was already updated by another thread.")
    }

    @Test
    fun `should not insert server status monitor when using updateServer`() {

        val e = assertThrows<OutdatedServerException> {
            serverRepository.updateServer(
                ServerTestUtils.getServer()
            )
        }

        assertThat(e.message).isEqualTo("Server with id '${ServerTestUtils.ID}' not found.")
    }
}
