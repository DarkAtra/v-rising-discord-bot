package de.darkatra.vrising.discord.clients.botcompanion.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.jacksonObjectMapper

class PlayerActivityTest {

    @Test
    fun `should deserialize player activity`() {

        val objectMapper = jacksonObjectMapper()

        val playerActivity = objectMapper.readValue(
            // language=json
            """
            {
              "type": "CONNECTED",
              "playerName": "Atra",
              "occurred": "2023-01-01T00:00:00Z"
            }
            """.trimIndent(),
            PlayerActivity::class.java
        )

        assertThat(playerActivity).isNotNull()
        assertThat(playerActivity.type).isEqualTo(PlayerActivity.Type.CONNECTED)
        assertThat(playerActivity.playerName).isEqualTo("Atra")
        assertThat(playerActivity.occurred).isEqualTo("2023-01-01T00:00:00Z")
    }
}
