package de.darkatra.vrising.discord.clients.botcompanion

import de.darkatra.vrising.discord.clients.botcompanion.model.Character
import de.darkatra.vrising.discord.clients.botcompanion.model.PlayerActivity
import de.darkatra.vrising.discord.clients.botcompanion.model.PvpKill
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.net.InetSocketAddress
import java.net.URI
import java.time.Duration

@Service
class BotCompanionClient {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun getCharacters(serverApiHostName: String, serverApiPort: Int, interceptors: List<ClientHttpRequestInterceptor>): List<Character> {

        val restTemplate = getRestTemplate(serverApiHostName, serverApiPort, interceptors)

        return try {
            restTemplate.getForObject("/characters", Array<Character>::class.java)?.toList() ?: emptyList()
        } catch (e: RestClientException) {
            logger.warn("Could not resolve characters for '${serverApiHostName}:${serverApiPort}'. Falling back to an empty list.", e)
            emptyList()
        }
    }

    fun getPlayerActivities(serverApiHostName: String, serverApiPort: Int, interceptors: List<ClientHttpRequestInterceptor>): List<PlayerActivity> {

        val restTemplate = getRestTemplate(serverApiHostName, serverApiPort, interceptors)

        return try {
            restTemplate.getForObject("/player-activities", Array<PlayerActivity>::class.java)?.toList() ?: emptyList()
        } catch (e: RestClientException) {
            logger.warn("Could not fetch player activities for '${serverApiHostName}:${serverApiPort}'. Falling back to an empty list.", e)
            emptyList()
        }
    }

    fun getPvpKills(serverApiHostName: String, serverApiPort: Int, interceptors: List<ClientHttpRequestInterceptor>): List<PvpKill> {

        val restTemplate = getRestTemplate(serverApiHostName, serverApiPort, interceptors)

        return try {
            restTemplate.getForObject("/pvp-kills", Array<PvpKill>::class.java)?.toList() ?: emptyList()
        } catch (e: RestClientException) {
            logger.warn("Could not fetch pvp kills for '${serverApiHostName}:${serverApiPort}'. Falling back to an empty list.", e)
            emptyList()
        }
    }

    private fun getRestTemplate(serverApiHostName: String, serverApiPort: Int, interceptors: List<ClientHttpRequestInterceptor>): RestTemplate {

        val address = InetSocketAddress(serverApiHostName, serverApiPort)

        @Suppress("HttpUrlsUsage") // the v risings http server does not support https
        val requestURI = URI.create("http://${address.hostString}:${address.port}/v-rising-discord-bot")

        return RestTemplateBuilder()
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(5))
            .rootUri(requestURI.toString())
            .interceptors(interceptors)
            .build()
    }
}
