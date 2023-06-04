package de.darkatra.vrising.discord.botcompanion

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import de.darkatra.vrising.discord.botcompanion.model.VBlood
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledInNativeImage
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

@DisabledInNativeImage
class BotCompanionClientTest {

    // workaroun for https://github.com/wiremock/wiremock/issues/2202
    companion object {
        private var wireMock: WireMockServer? = null

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            wireMock = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())
            wireMock!!.start()
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            wireMock?.stop()
        }
    }

    private val botCompanionClient = BotCompanionClient()

    @Test
    fun `should get characters`() {

        wireMock!!.stubFor(
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

        val characters = botCompanionClient.getCharacters("localhost", wireMock!!.port())
        assertThat(characters).isNotEmpty()

        val character = characters.first()
        assertThat(character.name).isEqualTo("Atra")
        assertThat(character.gearLevel).isEqualTo(83)
        assertThat(character.clan).isEqualTo("Test")
        assertThat(character.killedVBloods).containsExactlyInAnyOrder(VBlood.FOREST_WOLF, VBlood.BANDIT_STONEBREAKER)
    }

    @Test
    fun `should handle errors getting characters`() {

        wireMock!!.stubFor(
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

        val characters = botCompanionClient.getCharacters("localhost", wireMock!!.port())
        assertThat(characters).isEmpty()
    }
}
