package de.darkatra.vrising.discord.clients.botcompanion

import de.darkatra.vrising.discord.clients.botcompanion.model.Character
import de.darkatra.vrising.discord.clients.botcompanion.model.PlayerActivity
import de.darkatra.vrising.discord.clients.botcompanion.model.PvpKill
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.net.InetSocketAddress
import java.net.URI
import java.time.Duration

@Service
class BotCompanionClient {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val restTemplate: RestTemplate = RestTemplateBuilder()
        .setConnectTimeout(Duration.ofSeconds(5))
        .setReadTimeout(Duration.ofSeconds(5))
        .build()

    fun getCharacters(serverHostName: String, serverApiPort: Int): List<Character> {

        val address = InetSocketAddress(serverHostName, serverApiPort)

        @Suppress("HttpUrlsUsage") // the v risings http server does not support https
        val requestURI = URI.create("http://${address.hostString}:${address.port}/v-rising-discord-bot/characters")

        return try {
            restTemplate.getForObject(requestURI, Array<Character>::class.java)?.toList() ?: emptyList()
        } catch (e: RestClientException) {
            logger.warn("Could not resolve characters for '${address.hostString}:${address.port}'. Falling back to an empty list.", e)
            emptyList()
        }
    }

    fun getPlayerActivities(serverApiHostName: String, serverApiPort: Int): List<PlayerActivity> {

        val address = InetSocketAddress(serverApiHostName, serverApiPort)

        @Suppress("HttpUrlsUsage") // the v risings http server does not support https
        val requestURI = URI.create("http://${address.hostString}:${address.port}/v-rising-discord-bot/player-activities")

        return try {
            restTemplate.getForObject(requestURI, Array<PlayerActivity>::class.java)?.toList() ?: emptyList()
        } catch (e: RestClientException) {
            logger.warn("Could not fetch player activities for '${address.hostString}:${address.port}'. Falling back to an empty list.", e)
            emptyList()
        }
    }

    fun getPvpKills(serverApiHostName: String, serverApiPort: Int): List<PvpKill> {

        val address = InetSocketAddress(serverApiHostName, serverApiPort)

        @Suppress("HttpUrlsUsage") // the v risings http server does not support https
        val requestURI = URI.create("http://${address.hostString}:${address.port}/v-rising-discord-bot/pvp-kills")

        return try {
            restTemplate.getForObject(requestURI, Array<PvpKill>::class.java)?.toList() ?: emptyList()
        } catch (e: RestClientException) {
            logger.warn("Could not fetch pvp kills for '${address.hostString}:${address.port}'. Falling back to an empty list.", e)
            emptyList()
        }
    }
}
