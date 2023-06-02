package de.darkatra.vrising.discord.botcompanion

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.darkatra.vrising.discord.botcompanion.model.CharacterResponse
import org.springframework.stereotype.Service
import java.net.InetSocketAddress
import java.net.URI

// TODO: tests
@Service
class BotCompanionClient {

    private val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    fun getCharacters(serverHostName: String, serverApiPort: Int): List<CharacterResponse> {
        val address = InetSocketAddress(serverHostName, serverApiPort)

        // TODO: maybe use an http client?
        @Suppress("HttpUrlsUsage") // the v risings http server does not support https
        val requestURI = URI.create("http://${address.hostString}:${address.port}/v-rising-discord-bot/characters").toURL()

        return objectMapper.readValue<List<CharacterResponse>>(requestURI.openStream())
    }
}
