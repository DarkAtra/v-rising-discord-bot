package de.darkatra.vrising.discord.clients.botcompanion

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import de.darkatra.vrising.discord.clients.botcompanion.model.Character
import de.darkatra.vrising.discord.clients.botcompanion.model.PlayerActivity
import de.darkatra.vrising.discord.clients.botcompanion.model.PvpKill
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.accept
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import io.ktor.http.headers
import io.ktor.http.userAgent
import org.springframework.beans.factory.DisposableBean
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.net.InetSocketAddress
import java.net.URL
import java.time.Duration

@Service
class BotCompanionClient(
    private val applicationContext: ApplicationContext
) : DisposableBean {

    private val objectMapper by lazy {
        jacksonObjectMapper().registerModule(JavaTimeModule())
    }
    private val httpClient by lazy {
        HttpClient(OkHttp) {
            install(HttpTimeout) {
                connectTimeoutMillis = Duration.ofSeconds(1).toMillis()
                requestTimeoutMillis = Duration.ofSeconds(5).toMillis()
                socketTimeoutMillis = Duration.ofSeconds(5).toMillis()
            }
        }
    }

    suspend fun getCharacters(
        serverApiHostName: String,
        serverApiPort: Int,
        serverApiUsername: String? = null,
        serverApiPassword: String? = null
    ): Result<List<Character>> {

        val response = performRequest(getRequestUrl(serverApiHostName, serverApiPort), "/characters", serverApiUsername, serverApiPassword)

        return when (response.status) {
            HttpStatusCode.OK -> Result.success(objectMapper.readValue(response.bodyAsText(), jacksonTypeRef<List<Character>>()))
            else -> Result.failure(BotCompanionClientException("Unexpected response status '${response.status.value}' during ${this::getCharacters.name} request."))
        }
    }

    suspend fun getPlayerActivities(
        serverApiHostName: String,
        serverApiPort: Int,
        serverApiUsername: String? = null,
        serverApiPassword: String? = null
    ): Result<List<PlayerActivity>> {

        val response = performRequest(getRequestUrl(serverApiHostName, serverApiPort), "/player-activities", serverApiUsername, serverApiPassword)

        return when (response.status) {
            HttpStatusCode.OK -> Result.success(objectMapper.readValue(response.bodyAsText(), jacksonTypeRef<List<PlayerActivity>>()))
            else -> Result.failure(BotCompanionClientException("Unexpected response status '${response.status.value}' during ${this::getPlayerActivities.name} request."))
        }
    }

    suspend fun getPvpKills(
        serverApiHostName: String,
        serverApiPort: Int,
        serverApiUsername: String? = null,
        serverApiPassword: String? = null
    ): Result<List<PvpKill>> {

        val response = performRequest(getRequestUrl(serverApiHostName, serverApiPort), "/pvp-kills", serverApiUsername, serverApiPassword)

        return when (response.status) {
            HttpStatusCode.OK -> Result.success(objectMapper.readValue(response.bodyAsText(), jacksonTypeRef<List<PvpKill>>()))
            else -> Result.failure(BotCompanionClientException("Unexpected response status '${response.status.value}' during ${this::getPvpKills.name} request."))
        }
    }

    private suspend fun performRequest(url: URL, path: String, serverApiUsername: String?, serverApiPassword: String?): HttpResponse {
        return httpClient.get(url) {
            url {
                appendPathSegments(path)
            }
            headers {
                accept(ContentType.parse(MediaType.APPLICATION_JSON_VALUE))
                applicationContext.id?.let { userAgent(it) }
                if (serverApiUsername != null && serverApiPassword != null) {
                    basicAuth(serverApiUsername, serverApiPassword)
                }
            }
        }
    }

    private fun getRequestUrl(serverApiHostName: String, serverApiPort: Int): URL {
        val address = InetSocketAddress(serverApiHostName, serverApiPort)
        return URL("http://${address.hostString}:${address.port}/v-rising-discord-bot")
    }

    override fun destroy() {
        httpClient.close()
    }
}
