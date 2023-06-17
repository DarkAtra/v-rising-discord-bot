package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.serverstatus.model.ServerInfoTestUtils
import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ServerStatusEmbedTest {

    @Test
    fun `should build embed`() {

        val serverInfo = ServerInfoTestUtils.getServerInfo()
        val embedBuilder = EmbedBuilder()

        ServerStatusEmbed.buildEmbed(
            serverInfo = serverInfo,
            apiEnabled = false,
            displayServerDescription = true,
            displayPlayerGearLevel = false,
            embedBuilder = embedBuilder
        )

        assertThat(embedBuilder.title).isEqualTo(ServerInfoTestUtils.NAME)
        assertThat(embedBuilder.color).isEqualTo(Color(red = 0, green = 142, blue = 68))
        assertThat(embedBuilder.description).isEqualTo(ServerInfoTestUtils.DESCRIPTION)
        assertThat(fieldByName(embedBuilder, "Ip and Port").value).isEqualTo("${ServerInfoTestUtils.IP}:${ServerInfoTestUtils.GAME_PORT}")
        assertThat(fieldByName(embedBuilder, "Online count").value).isEqualTo("${ServerInfoTestUtils.NUMBER_OF_PLAYERS}/${ServerInfoTestUtils.MAX_PLAYERS}")
        assertThat(fieldByName(embedBuilder, "Days running").value).isEqualTo(ServerInfoTestUtils.DAYS_RUNNING.toString())
        assertThat(fieldByName(embedBuilder, "Online players").value).isEqualTo("**Atra**")
    }

    private fun fieldByName(embedBuilder: EmbedBuilder, fieldName: String): EmbedBuilder.Field {
        return embedBuilder.fields.first { field ->
            field.name == fieldName
        }
    }
}
