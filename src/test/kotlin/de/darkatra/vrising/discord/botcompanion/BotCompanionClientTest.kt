package de.darkatra.vrising.discord.botcompanion

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import de.darkatra.vrising.discord.botcompanion.model.VBlood
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledInNativeImage
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

@WireMockTest
@DisabledInNativeImage
class BotCompanionClientTest {

    private val botCompanionClient = BotCompanionClient()

    @Test
    fun `should get characters`(wireMockRuntimeInfo: WireMockRuntimeInfo) {

        val wireMock = wireMockRuntimeInfo.wireMock

        wireMock.register(
            WireMock.get("/v-rising-discord-bot/characters")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                            """
                            [
                              {
                                "name": "Atra",
                                "gearLevel": 83,
                                "clan": "Test",
                                "killedVBloods": [
                                  "FOREST_WOLF",
                                  "BANDIT_STONEBREAKER"
                                ]
                              }
                            ]""".trimIndent()
                        )
                )
        )

        val characters = botCompanionClient.getCharacters("localhost", wireMockRuntimeInfo.httpPort)
        assertThat(characters).isNotEmpty()

        val character = characters.first()
        assertThat(character.name).isEqualTo("Atra")
        assertThat(character.gearLevel).isEqualTo(83)
        assertThat(character.clan).isEqualTo("Test")
        assertThat(character.killedVBloods).containsExactlyInAnyOrder(VBlood.FOREST_WOLF, VBlood.BANDIT_STONEBREAKER)
    }

    @Test
    fun `should handle errors getting characters`(wireMockRuntimeInfo: WireMockRuntimeInfo) {

        val wireMock = wireMockRuntimeInfo.wireMock

        wireMock.register(
            WireMock.get("/v-rising-discord-bot/characters")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                            """
                            {
                              "type": "about:blank",
                              "title": "Internal Server Error"
                            }""".trimIndent()
                        )
                )
        )

        val characters = botCompanionClient.getCharacters("localhost", wireMockRuntimeInfo.httpPort)
        assertThat(characters).isEmpty()
    }
}
