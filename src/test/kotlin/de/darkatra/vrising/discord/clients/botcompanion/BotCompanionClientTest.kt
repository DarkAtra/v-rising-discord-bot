package de.darkatra.vrising.discord.clients.botcompanion

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import de.darkatra.vrising.discord.clients.botcompanion.model.PlayerActivity
import de.darkatra.vrising.discord.clients.botcompanion.model.VBlood
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledInNativeImage
import org.springframework.context.support.StaticApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

@WireMockTest
@DisabledInNativeImage
class BotCompanionClientTest {

    private val botCompanionClient = BotCompanionClient(
        StaticApplicationContext().apply {
            id = "test"
        }
    )

    @Test
    fun `should handle timeouts correctly`(wireMockRuntimeInfo: WireMockRuntimeInfo) {

        wireMockRuntimeInfo.wireMock.register(
            WireMock.get("/v-rising-discord-bot/characters")
                .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.USER_AGENT, equalTo("test"))
                .willReturn(
                    WireMock.aResponse()
                        .withFixedDelay(10_000)
                        .withStatus(HttpStatus.OK.value())
                )
        )

        val charactersResult = runBlocking {
            botCompanionClient.getCharacters(
                serverApiHostName = "localhost",
                serverApiPort = wireMockRuntimeInfo.httpPort,
                useSecureTransport = false
            )
        }
        assertThat(charactersResult.isFailure).isTrue()

        val exception = charactersResult.exceptionOrNull()
        assertThat(exception).hasMessageContaining("Unexpected exception performing")
    }

    @Test
    fun `should get characters`(wireMockRuntimeInfo: WireMockRuntimeInfo) {

        wireMockRuntimeInfo.wireMock.register(
            WireMock.get("/v-rising-discord-bot/characters")
                .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.USER_AGENT, equalTo("test"))
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
                                  null
                                ]
                              }
                            ]""".trimIndent()
                        )
                )
        )

        val charactersResult = runBlocking {
            botCompanionClient.getCharacters(
                serverApiHostName = "localhost",
                serverApiPort = wireMockRuntimeInfo.httpPort,
                useSecureTransport = false
            )
        }
        assertThat(charactersResult.isSuccess).withFailMessage {
            charactersResult.exceptionOrNull()?.let { "${it.message}: ${it.stackTraceToString()}" }
        }.isTrue()

        val characters = charactersResult.getOrThrow()
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
                .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.USER_AGENT, equalTo("test"))
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

        val charactersResult = runBlocking {
            botCompanionClient.getCharacters(
                serverApiHostName = "localhost",
                serverApiPort = wireMockRuntimeInfo.httpPort,
                serverApiUsername = username,
                serverApiPassword = password,
                useSecureTransport = false
            )
        }
        assertThat(charactersResult.isSuccess).withFailMessage {
            charactersResult.exceptionOrNull()?.let { "${it.message}: ${it.stackTraceToString()}" }
        }.isTrue()

        val characters = charactersResult.getOrThrow()
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
                .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.USER_AGENT, equalTo("test"))
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

        val charactersResult = runBlocking {
            botCompanionClient.getCharacters(
                serverApiHostName = "localhost",
                serverApiPort = wireMockRuntimeInfo.httpPort,
                useSecureTransport = false
            )
        }
        assertThat(charactersResult.isFailure).isTrue()
    }

    @Test
    fun `should get player activities`(wireMockRuntimeInfo: WireMockRuntimeInfo) {

        wireMockRuntimeInfo.wireMock.register(
            WireMock.get("/v-rising-discord-bot/player-activities")
                .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.USER_AGENT, equalTo("test"))
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

        val playerActivitiesResult = runBlocking {
            botCompanionClient.getPlayerActivities(
                serverApiHostName = "localhost",
                serverApiPort = wireMockRuntimeInfo.httpPort,
                useSecureTransport = false
            )
        }
        assertThat(playerActivitiesResult.isSuccess).withFailMessage {
            playerActivitiesResult.exceptionOrNull()?.let { "${it.message}: ${it.stackTraceToString()}" }
        }.isTrue()

        val playerActivities = playerActivitiesResult.getOrThrow()
        assertThat(playerActivities).isNotEmpty()

        val character = playerActivities.first()
        assertThat(character.type).isEqualTo(PlayerActivity.Type.CONNECTED)
        assertThat(character.playerName).isEqualTo("Atra")
        assertThat(character.occurred.toString()).isEqualTo("2023-01-01T00:00:00Z")
    }

    @Test
    fun `should get pvp kills`(wireMockRuntimeInfo: WireMockRuntimeInfo) {

        wireMockRuntimeInfo.wireMock.register(
            WireMock.get("/v-rising-discord-bot/pvp-kills")
                .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.USER_AGENT, equalTo("test"))
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

        val pvpKillsResult = runBlocking {
            botCompanionClient.getPvpKills(
                serverApiHostName = "localhost",
                serverApiPort = wireMockRuntimeInfo.httpPort,
                useSecureTransport = false
            )
        }
        assertThat(pvpKillsResult.isSuccess).withFailMessage {
            pvpKillsResult.exceptionOrNull()?.let { "${it.message}: ${it.stackTraceToString()}" }
        }.isTrue()

        val pvpKills = pvpKillsResult.getOrThrow()
        assertThat(pvpKills).isNotEmpty()

        val character = pvpKills.first()
        assertThat(character.killer.name).isEqualTo("Atra")
        assertThat(character.killer.gearLevel).isEqualTo(71)
        assertThat(character.victim.name).isEqualTo("Testi")
        assertThat(character.victim.gearLevel).isEqualTo(11)
        assertThat(character.occurred.toString()).isEqualTo("2023-01-01T00:00:00Z")
    }

    @Test
    fun `should get raids`(wireMockRuntimeInfo: WireMockRuntimeInfo) {

        wireMockRuntimeInfo.wireMock.register(
            WireMock.get("/v-rising-discord-bot/raids")
                .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.USER_AGENT, equalTo("test"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                            // language=json
                            """
                            [
                              {
                                "attackers": [
                                  {
                                    "name": "Socium",
                                    "gearLevel": 91,
                                    "joinedAt": "2023-01-01T00:00:00Z"
                                  }
                                ],
                                "defenders": [
                                  {
                                    "name": "Atra",
                                    "gearLevel": 87,
                                    "clan": "Test",
                                    "joinedAt": "2023-01-02T00:00:00Z"
                                  }
                                ],
                                "occurred": "2023-01-01T00:00:00Z",
                                "updated": "2023-01-02T00:00:00Z"
                              }
                            ]""".trimIndent()
                        )
                )
        )

        val raidsResult = runBlocking {
            botCompanionClient.getRaids(
                serverApiHostName = "localhost",
                serverApiPort = wireMockRuntimeInfo.httpPort,
                useSecureTransport = false
            )
        }
        assertThat(raidsResult.isSuccess).withFailMessage {
            raidsResult.exceptionOrNull()?.let { "${it.message}: ${it.stackTraceToString()}" }
        }.isTrue()

        val raids = raidsResult.getOrThrow()
        assertThat(raids).isNotEmpty()

        val character = raids.first()
        assertThat(character.attackers).hasSize(1)
        assertThat(character.attackers.first().name).isEqualTo("Socium")
        assertThat(character.attackers.first().gearLevel).isEqualTo(91)
        assertThat(character.attackers.first().clan).isNull()
        assertThat(character.attackers.first().joinedAt).isEqualTo("2023-01-01T00:00:00Z")
        assertThat(character.defenders).hasSize(1)
        assertThat(character.defenders.first().name).isEqualTo("Atra")
        assertThat(character.defenders.first().gearLevel).isEqualTo(87)
        assertThat(character.defenders.first().clan).isEqualTo("Test")
        assertThat(character.defenders.first().joinedAt).isEqualTo("2023-01-02T00:00:00Z")
        assertThat(character.occurred.toString()).isEqualTo("2023-01-01T00:00:00Z")
        assertThat(character.updated.toString()).isEqualTo("2023-01-02T00:00:00Z")
    }

    @Test
    fun `should get raids from older bot companion versions`(wireMockRuntimeInfo: WireMockRuntimeInfo) {

        wireMockRuntimeInfo.wireMock.register(
            WireMock.get("/v-rising-discord-bot/raids")
                .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.USER_AGENT, equalTo("test"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                            // language=json
                            """
                            [
                              {
                                "attackers": [
                                  {
                                    "name": "Socium",
                                    "gearLevel": 91
                                  }
                                ],
                                "defenders": [
                                  {
                                    "name": "Atra",
                                    "gearLevel": 87
                                  }
                                ],
                                "occurred": "2023-01-01T00:00:00Z"
                              }
                            ]""".trimIndent()
                        )
                )
        )

        val raidsResult = runBlocking {
            botCompanionClient.getRaids(
                serverApiHostName = "localhost",
                serverApiPort = wireMockRuntimeInfo.httpPort,
                useSecureTransport = false
            )
        }
        assertThat(raidsResult.isSuccess).withFailMessage {
            raidsResult.exceptionOrNull()?.let { "${it.message}: ${it.stackTraceToString()}" }
        }.isTrue()

        val raids = raidsResult.getOrThrow()
        assertThat(raids).isNotEmpty()

        val character = raids.first()
        assertThat(character.attackers).hasSize(1)
        assertThat(character.attackers.first().name).isEqualTo("Socium")
        assertThat(character.attackers.first().gearLevel).isEqualTo(91)
        assertThat(character.attackers.first().clan).isNull()
        assertThat(character.attackers.first().joinedAt).isNull()
        assertThat(character.defenders).hasSize(1)
        assertThat(character.defenders.first().name).isEqualTo("Atra")
        assertThat(character.defenders.first().gearLevel).isEqualTo(87)
        assertThat(character.defenders.first().clan).isNull()
        assertThat(character.defenders.first().joinedAt).isNull()
        assertThat(character.occurred.toString()).isEqualTo("2023-01-01T00:00:00Z")
        assertThat(character.updated).isNull()
    }

    @Test
    fun `should get vblood kills`(wireMockRuntimeInfo: WireMockRuntimeInfo) {

        wireMockRuntimeInfo.wireMock.register(
            WireMock.get("/v-rising-discord-bot/vblood-kills")
                .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.USER_AGENT, equalTo("test"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                            // language=json
                            """
                            [
                              {
                                "killers": [
                                  {
                                    "name": "Atra",
                                    "gearLevel": 87
                                  },
                                  {
                                    "name": "Socium",
                                    "gearLevel": 91
                                  }
                                ],
                                "vBlood": "FOREST_BEAR_DIRE",
                                "occurred": "2023-01-01T00:00:00Z"
                              }
                            ]""".trimIndent()
                        )
                )
        )

        val vBloodKillsResult = runBlocking {
            botCompanionClient.getVBloodKills(
                serverApiHostName = "localhost",
                serverApiPort = wireMockRuntimeInfo.httpPort,
                useSecureTransport = false
            )
        }
        assertThat(vBloodKillsResult.isSuccess).withFailMessage {
            vBloodKillsResult.exceptionOrNull()?.let { "${it.message}: ${it.stackTraceToString()}" }
        }.isTrue()

        val vBloodKills = vBloodKillsResult.getOrThrow()
        assertThat(vBloodKills).isNotEmpty()

        val character = vBloodKills.first()
        assertThat(character.vBlood).isEqualTo(VBlood.FOREST_BEAR_DIRE)
        assertThat(character.killers).hasSize(2)
        assertThat(character.killers.first().name).isEqualTo("Atra")
        assertThat(character.killers.first().gearLevel).isEqualTo(87)
        assertThat(character.killers.last().name).isEqualTo("Socium")
        assertThat(character.killers.last().gearLevel).isEqualTo(91)
        assertThat(character.occurred.toString()).isEqualTo("2023-01-01T00:00:00Z")
    }
}
