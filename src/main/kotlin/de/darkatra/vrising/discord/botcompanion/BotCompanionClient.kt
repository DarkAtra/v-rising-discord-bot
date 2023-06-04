package de.darkatra.vrising.discord.botcompanion

import de.darkatra.vrising.discord.botcompanion.model.CharacterResponse
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
        .setConnectTimeout(Duration.ofSeconds(10))
        .setReadTimeout(Duration.ofSeconds(10))
        .build()

    fun getCharacters(serverHostName: String, serverApiPort: Int): List<CharacterResponse> {

        val address = InetSocketAddress(serverHostName, serverApiPort)

        @Suppress("HttpUrlsUsage") // the v risings http server does not support https
        val requestURI = URI.create("http://${address.hostString}:${address.port}/v-rising-discord-bot/characters")

        return try {
            restTemplate.getForObject(requestURI, Array<CharacterResponse>::class.java)?.toList() ?: emptyList()
        } catch (e: RestClientException) {
            logger.warn("Could not resolve characters for '${address.hostString}:${address.port}'. Falling back to an empty list.", e)
            emptyList()
        }
    }
}
