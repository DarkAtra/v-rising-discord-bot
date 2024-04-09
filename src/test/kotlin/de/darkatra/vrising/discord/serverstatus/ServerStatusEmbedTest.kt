package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.serverstatus.model.ServerInfoTestUtils
import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.datetime.toJavaInstant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class ServerStatusEmbedTest {

    @Test
    fun `should build embed`() {

        val serverInfo = ServerInfoTestUtils.getServerInfo()
        val embedBuilder = EmbedBuilder()

        val timestampBeforeEmbed = Instant.now()

        ServerStatusEmbed.buildEmbed(
            serverInfo = serverInfo,
            apiEnabled = false,
            displayServerDescription = true,
            displayPlayerGearLevel = false,
            embedBuilder = embedBuilder
        )

        val timestampAfterEmbed = Instant.now()

        assertThat(embedBuilder.title).isEqualTo(ServerInfoTestUtils.NAME)
        assertThat(embedBuilder.color).isEqualTo(Color(red = 0, green = 142, blue = 68))
        assertThat(embedBuilder.description).isEqualTo(ServerInfoTestUtils.DESCRIPTION)
        assertThat(fieldByName(embedBuilder, "Ip and Port").value).isEqualTo("${ServerInfoTestUtils.IP}:${ServerInfoTestUtils.GAME_PORT}")
        assertThat(fieldByName(embedBuilder, "Online count").value).isEqualTo("${ServerInfoTestUtils.NUMBER_OF_PLAYERS}/${ServerInfoTestUtils.MAX_PLAYERS}")
        assertThat(fieldByName(embedBuilder, "Days running").value).isEqualTo(ServerInfoTestUtils.DAYS_RUNNING.toString())
        assertThat(fieldByName(embedBuilder, "Online players").value).isEqualTo("**Atra**")
        assertThat(embedBuilder.timestamp).isNotNull
        assertThat(embedBuilder.timestamp!!.toJavaInstant()).isAfter(timestampBeforeEmbed)
        assertThat(embedBuilder.timestamp!!.toJavaInstant()).isBefore(timestampAfterEmbed)
    }

    private fun fieldByName(embedBuilder: EmbedBuilder, fieldName: String): EmbedBuilder.Field {
        return embedBuilder.fields.first { field ->
            field.name == fieldName
        }
    }
}
