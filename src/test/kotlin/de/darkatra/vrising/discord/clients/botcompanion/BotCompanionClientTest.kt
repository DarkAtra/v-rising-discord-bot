package de.darkatra.vrising.discord.clients.botcompanion

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import de.darkatra.vrising.discord.clients.botcompanion.model.PlayerActivity
import de.darkatra.vrising.discord.clients.botcompanion.model.VBlood
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledInNativeImage
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.support.BasicAuthenticationInterceptor

@WireMockTest
@DisabledInNativeImage
class BotCompanionClientTest {

    private val botCompanionClient = BotCompanionClient()

    @Test
    fun `should get characters`(wireMockRuntimeInfo: WireMockRuntimeInfo) {

        wireMockRuntimeInfo.wireMock.register(
            WireMock.get("/v-rising-discord-bot/characters")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                            // language=json
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

        val characters = botCompanionClient.getCharacters("localhost", wireMockRuntimeInfo.httpPort, emptyList())
        assertThat(characters).isNotEmpty()

        val character = characters.first()
        assertThat(character.name).isEqualTo("Atra")
        assertThat(character.gearLevel).isEqualTo(83)
        assertThat(character.clan).isEqualTo("Test")
        assertThat(character.killedVBloods).containsExactlyInAnyOrder(VBlood.FOREST_WOLF, VBlood.BANDIT_STONEBREAKER)
    }

    @Test
    fun `should get characters with basic authentication`(wireMockRuntimeInfo: WireMockRuntimeInfo) {

        val username = "test"
        val password = "password"

        wireMockRuntimeInfo.wireMock.register(
            WireMock.get("/v-rising-discord-bot/characters")
                .withBasicAuth(username, password)
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                            // language=json
                            """
                            [
                              {
                                "name": "Atra",
                                "gearLevel": 83,
                                "clan": "Test",
                                "killedVBloods": [
                                  "FOREST_WOLF",
                                  "BANDIT_STONEBREAKER",
                                  "DRACULA"
                                ]
                              }
                            ]""".trimIndent()
                        )
                )
        )

        val characters = botCompanionClient.getCharacters(
            "localhost",
            wireMockRuntimeInfo.httpPort,
            listOf(BasicAuthenticationInterceptor(username, password))
        )
        assertThat(characters).isNotEmpty()

        val character = characters.first()
        assertThat(character.name).isEqualTo("Atra")
        assertThat(character.gearLevel).isEqualTo(83)
        assertThat(character.clan).isEqualTo("Test")
        assertThat(character.killedVBloods).containsExactlyInAnyOrder(VBlood.FOREST_WOLF, VBlood.BANDIT_STONEBREAKER, VBlood.DRACULA)
    }

    @Test
    fun `should handle errors getting characters`(wireMockRuntimeInfo: WireMockRuntimeInfo) {

        wireMockRuntimeInfo.wireMock.register(
            WireMock.get("/v-rising-discord-bot/characters")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                            // language=json
                            """
                            {
                              "type": "about:blank",
                              "title": "Internal Server Error"
                            }""".trimIndent()
                        )
                )
        )

        val characters = botCompanionClient.getCharacters("localhost", wireMockRuntimeInfo.httpPort, emptyList())
        assertThat(characters).isEmpty()
    }

    @Test
    fun `should get player activities`(wireMockRuntimeInfo: WireMockRuntimeInfo) {

        val wireMock = wireMockRuntimeInfo.wireMock

        wireMock.register(
            WireMock.get("/v-rising-discord-bot/player-activities")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                            // language=json
                            """
                            [
                              {
                                "type": "CONNECTED",
                                "playerName": "Atra",
                                "occurred": "2023-01-01T00:00:00Z"
                              },
                              {
                                "type": "DISCONNECTED",
                                "playerName": "Atra",
                                "occurred": "2023-01-01T01:00:00Z"
                              }
                            ]""".trimIndent()
                        )
                )
        )

        val playerActivities = botCompanionClient.getPlayerActivities("localhost", wireMockRuntimeInfo.httpPort, emptyList())
        assertThat(playerActivities).isNotEmpty()

        val character = playerActivities.first()
        assertThat(character.type).isEqualTo(PlayerActivity.Type.CONNECTED)
        assertThat(character.playerName).isEqualTo("Atra")
        assertThat(character.occurred.toString()).isEqualTo("2023-01-01T00:00:00Z")
    }

    @Test
    fun `should get pvp kills`(wireMockRuntimeInfo: WireMockRuntimeInfo) {

        val wireMock = wireMockRuntimeInfo.wireMock

        wireMock.register(
            WireMock.get("/v-rising-discord-bot/pvp-kills")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                            // language=json
                            """
                            [
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
                            ]""".trimIndent()
                        )
                )
        )

        val pvpKills = botCompanionClient.getPvpKills("localhost", wireMockRuntimeInfo.httpPort, emptyList())
        assertThat(pvpKills).isNotEmpty()

        val character = pvpKills.first()
        assertThat(character.killer.name).isEqualTo("Atra")
        assertThat(character.killer.gearLevel).isEqualTo(71)
        assertThat(character.victim.name).isEqualTo("Testi")
        assertThat(character.victim.gearLevel).isEqualTo(11)
        assertThat(character.occurred.toString()).isEqualTo("2023-01-01T00:00:00Z")
    }
}
