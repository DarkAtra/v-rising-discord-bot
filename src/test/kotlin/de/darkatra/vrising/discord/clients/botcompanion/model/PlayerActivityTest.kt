package de.darkatra.vrising.discord.clients.botcompanion.model

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlayerActivityTest {

    @Test
    fun `should deserialize player activity`() {

        val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

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
