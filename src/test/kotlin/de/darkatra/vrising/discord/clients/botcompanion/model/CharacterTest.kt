package de.darkatra.vrising.discord.clients.botcompanion.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.jacksonObjectMapper

class CharacterTest {

    @Test
    fun `should deserialize character`() {

        val objectMapper = jacksonObjectMapper()

        val character = objectMapper.readValue(
            // language=json
            """
            {
              "name": "Atra",
              "gearLevel": 83,
              "clan": "Test",
              "killedVBloods": [
                "FOREST_WOLF",
                "BANDIT_STONEBREAKER",
                null
              ]
            }
            """.trimIndent(),
            Character::class.java
        )

        assertThat(character).isNotNull()
        assertThat(character.name).isEqualTo("Atra")
        assertThat(character.gearLevel).isEqualTo(83)
        assertThat(character.clan).isEqualTo("Test")
        assertThat(character.killedVBloods).containsExactlyInAnyOrder(VBlood.FOREST_WOLF, VBlood.BANDIT_STONEBREAKER)
    }
}
