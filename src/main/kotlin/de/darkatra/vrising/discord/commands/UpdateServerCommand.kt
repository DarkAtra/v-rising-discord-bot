package de.darkatra.vrising.discord.commands

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.commands.parameters.ServerApiHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.ServerHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.addServerApiHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.addServerApiPasswordParameter
import de.darkatra.vrising.discord.commands.parameters.addServerApiPortParameter
import de.darkatra.vrising.discord.commands.parameters.addServerApiUsernameParameter
import de.darkatra.vrising.discord.commands.parameters.addServerHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.addServerIdParameter
import de.darkatra.vrising.discord.commands.parameters.addServerQueryPortParameter
import de.darkatra.vrising.discord.commands.parameters.addUseSecureTransportParameter
import de.darkatra.vrising.discord.commands.parameters.getServerApiHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.getServerApiPasswordParameter
import de.darkatra.vrising.discord.commands.parameters.getServerApiPortParameter
import de.darkatra.vrising.discord.commands.parameters.getServerApiUsernameParameter
import de.darkatra.vrising.discord.commands.parameters.getServerHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.getServerIdParameter
import de.darkatra.vrising.discord.commands.parameters.getServerQueryPortParameter
import de.darkatra.vrising.discord.commands.parameters.getUseSecureTransportParameter
import de.darkatra.vrising.discord.persistence.ServerRepository
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

@Component
@EnableConfigurationProperties(BotProperties::class)
class UpdateServerCommand(
    private val serverRepository: ServerRepository,
    private val botProperties: BotProperties
) : Command {

    private val logger by lazy { LoggerFactory.getLogger(javaClass) }

    private val name: String = "update-server"
    private val description: String = "Updates the given server."

    override fun getCommandName(): String = name
    override fun getArgumentCount(): Int = 8

    override suspend fun register(kord: Kord) {

        kord.createGlobalChatInputCommand(
            name = name,
            description = description
        ) {
            dmPermission = true
            disableCommandInGuilds()

            addServerIdParameter()

            addServerHostnameParameter(required = false)
            addServerQueryPortParameter(required = false)

            addServerApiHostnameParameter(required = false)
            addServerApiPortParameter(required = false)
            addServerApiUsernameParameter(required = false)
            addServerApiPasswordParameter(required = false)

            addUseSecureTransportParameter(required = false)
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val server = interaction.getServer(serverRepository)
            ?: return

        val serverId = interaction.getServerIdParameter()
        val hostname = interaction.getServerHostnameParameter()
        val queryPort = interaction.getServerQueryPortParameter()

        val apiHostname = interaction.getServerApiHostnameParameter()
        val apiPort = interaction.getServerApiPortParameter()
        val apiUsername = interaction.getServerApiUsernameParameter()
        val apiPassword = interaction.getServerApiPasswordParameter()

        val useSecureTransport = interaction.getUseSecureTransportParameter()

        if (hostname != null) {
            ServerHostnameParameter.validate(hostname, botProperties.allowLocalAddressRanges)
            server.hostname = hostname
        }
        if (queryPort != null) {
            server.queryPort = queryPort
        }
        if (apiHostname != null) {
            server.apiHostname = determineValueOfNullableStringParameter(apiHostname).also {
                ServerApiHostnameParameter.validate(it, botProperties.allowLocalAddressRanges)
            }
        }
        if (apiPort != null) {
            server.apiPort = if (apiPort == -1) null else apiPort
        }
        if (apiUsername != null) {
            server.apiUsername = determineValueOfNullableStringParameter(apiUsername)
        }
        if (apiPassword != null) {
            server.apiPassword = determineValueOfNullableStringParameter(apiPassword)
        }
        if (useSecureTransport != null) {
            server.useSecureTransport = useSecureTransport
        }

        serverRepository.updateServer(server)

        logger.info("Successfully updated server '$serverId'.")

        interaction.deferEphemeralResponse().respond {
            content = """Successfully updated server with id '$serverId'.
                |Related status embeds, activity feeds, kill feeds and leaderboards may take some time to reflect the changes.""".trimMargin()
        }
    }

    private fun determineValueOfNullableStringParameter(value: String): String? {
        return when (value) {
            "~" -> null
            else -> value
        }
    }
}
