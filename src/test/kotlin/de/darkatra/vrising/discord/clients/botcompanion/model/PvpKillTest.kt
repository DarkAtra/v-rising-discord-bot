package de.darkatra.vrising.discord.clients.botcompanion.model

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PvpKillTest {

    @Test
    fun `should deserialize pvp kill`() {

        val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

        val pvpKill = objectMapper.readValue(
            // language=json
            """
            {
              "killer": {
                "name": "Atra",
                "gearLevel": 71
              },
              "victim": {
                "name": "Testi",
                "gearLevel": 11
              },
              "occurred": "2023-01-01T00:00:00Z"
            }
            """.trimIndent(),
            PvpKill::class.java
        )

        assertThat(pvpKill).isNotNull()
        assertThat(pvpKill.killer).isNotNull()
        assertThat(pvpKill.killer.name).isEqualTo("Atra")
        assertThat(pvpKill.killer.gearLevel).isEqualTo(71)
        assertThat(pvpKill.victim).isNotNull()
        assertThat(pvpKill.victim.name).isEqualTo("Testi")
        assertThat(pvpKill.victim.gearLevel).isEqualTo(11)
        assertThat(pvpKill.occurred).isEqualTo("2023-01-01T00:00:00Z")
    }
}
