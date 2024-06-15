package de.darkatra.vrising.discord.clients.botcompanion

import de.darkatra.vrising.discord.clients.botcompanion.model.Character
import de.darkatra.vrising.discord.clients.botcompanion.model.PlayerActivity
import de.darkatra.vrising.discord.clients.botcompanion.model.PvpKill
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.InetSocketAddress
import java.net.URI
import java.time.Duration

@Service
class BotCompanionClient {

    fun getCharacters(serverApiHostName: String, serverApiPort: Int, interceptors: List<ClientHttpRequestInterceptor>): Result<List<Character>> {

        val restTemplate = getRestTemplate(serverApiHostName, serverApiPort, interceptors)

        return try {
            Result.success(
                restTemplate.getForObject("/characters", Array<Character>::class.java)?.toList() ?: emptyList()
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPlayerActivities(serverApiHostName: String, serverApiPort: Int, interceptors: List<ClientHttpRequestInterceptor>): Result<List<PlayerActivity>> {

        val restTemplate = getRestTemplate(serverApiHostName, serverApiPort, interceptors)

        return try {
            Result.success(
                restTemplate.getForObject("/player-activities", Array<PlayerActivity>::class.java)?.toList() ?: emptyList()
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPvpKills(serverApiHostName: String, serverApiPort: Int, interceptors: List<ClientHttpRequestInterceptor>): Result<List<PvpKill>> {

        val restTemplate = getRestTemplate(serverApiHostName, serverApiPort, interceptors)

        return try {
            Result.success(
                restTemplate.getForObject("/pvp-kills", Array<PvpKill>::class.java)?.toList() ?: emptyList()
            )
        } catch (e: Exception) {
            Result.failure(e)
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
