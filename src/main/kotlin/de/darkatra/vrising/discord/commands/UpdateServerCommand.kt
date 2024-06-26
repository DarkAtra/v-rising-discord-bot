package de.darkatra.vrising.discord.commands

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.commands.parameters.ServerApiHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.ServerHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.addDisplayPlayerGearLevelParameter
import de.darkatra.vrising.discord.commands.parameters.addDisplayServerDescriptionParameter
import de.darkatra.vrising.discord.commands.parameters.addEmbedEnabledParameter
import de.darkatra.vrising.discord.commands.parameters.addPlayerActivityFeedChannelIdParameter
import de.darkatra.vrising.discord.commands.parameters.addPvpKillFeedChannelIdParameter
import de.darkatra.vrising.discord.commands.parameters.addServerApiHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.addServerApiPasswordParameter
import de.darkatra.vrising.discord.commands.parameters.addServerApiPortParameter
import de.darkatra.vrising.discord.commands.parameters.addServerApiUsernameParameter
import de.darkatra.vrising.discord.commands.parameters.addServerHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.addServerQueryPortParameter
import de.darkatra.vrising.discord.commands.parameters.addServerStatusMonitorIdParameter
import de.darkatra.vrising.discord.commands.parameters.addServerStatusMonitorStatusParameter
import de.darkatra.vrising.discord.commands.parameters.getDisplayPlayerGearLevelParameter
import de.darkatra.vrising.discord.commands.parameters.getDisplayServerDescriptionParameter
import de.darkatra.vrising.discord.commands.parameters.getEmbedEnabledParameter
import de.darkatra.vrising.discord.commands.parameters.getPlayerActivityFeedChannelIdParameter
import de.darkatra.vrising.discord.commands.parameters.getPvpKillFeedChannelIdParameter
import de.darkatra.vrising.discord.commands.parameters.getServerApiHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.getServerApiPasswordParameter
import de.darkatra.vrising.discord.commands.parameters.getServerApiPortParameter
import de.darkatra.vrising.discord.commands.parameters.getServerApiUsernameParameter
import de.darkatra.vrising.discord.commands.parameters.getServerHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.getServerQueryPortParameter
import de.darkatra.vrising.discord.commands.parameters.getServerStatusMonitorIdParameter
import de.darkatra.vrising.discord.commands.parameters.getServerStatusMonitorStatusParameter
import de.darkatra.vrising.discord.persistence.ServerStatusMonitorRepository
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GlobalChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

@Component
@EnableConfigurationProperties(BotProperties::class)
class UpdateServerCommand(
    private val serverStatusMonitorRepository: ServerStatusMonitorRepository,
    private val botProperties: BotProperties
) : Command {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val name: String = "update-server"
    private val description: String = "Updates the given server status monitor."

    override fun getCommandName(): String = name

    override suspend fun register(kord: Kord) {

        kord.createGlobalChatInputCommand(
            name = name,
            description = description
        ) {
            dmPermission = true
            disableCommandInGuilds()

            addServerStatusMonitorIdParameter()

            addServerHostnameParameter(required = false)
            addServerQueryPortParameter(required = false)

            addServerApiHostnameParameter(required = false)
            addServerApiPortParameter(required = false)
            addServerApiUsernameParameter(required = false)
            addServerApiPasswordParameter(required = false)

            addServerStatusMonitorStatusParameter(required = false)

            addEmbedEnabledParameter(required = false)
            addDisplayServerDescriptionParameter(required = false)
            addDisplayPlayerGearLevelParameter(required = false)

            addPlayerActivityFeedChannelIdParameter(required = false)
            addPvpKillFeedChannelIdParameter(required = false)
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val serverStatusMonitorId = interaction.getServerStatusMonitorIdParameter()
        val hostname = interaction.getServerHostnameParameter()
        val queryPort = interaction.getServerQueryPortParameter()

        val apiHostname = interaction.getServerApiHostnameParameter()
        val apiPort = interaction.getServerApiPortParameter()
        val apiUsername = interaction.getServerApiUsernameParameter()
        val apiPassword = interaction.getServerApiPasswordParameter()

        val status = interaction.getServerStatusMonitorStatusParameter()

        val embedEnabled = interaction.getEmbedEnabledParameter()
        val displayServerDescription = interaction.getDisplayServerDescriptionParameter()
        val displayPlayerGearLevel = interaction.getDisplayPlayerGearLevelParameter()

        val playerActivityFeedChannelId = interaction.getPlayerActivityFeedChannelIdParameter()
        val pvpKillFeedChannelId = interaction.getPvpKillFeedChannelIdParameter()

        val serverStatusMonitor = when (interaction) {
            is GuildChatInputCommandInteraction -> serverStatusMonitorRepository.getServerStatusMonitor(serverStatusMonitorId, interaction.guildId.toString())
            is GlobalChatInputCommandInteraction -> serverStatusMonitorRepository.getServerStatusMonitor(serverStatusMonitorId)
        }

        if (serverStatusMonitor == null) {
            interaction.deferEphemeralResponse().respond {
                content = "No server with id '$serverStatusMonitorId' was found."
            }
            return
        }

        if (hostname != null) {
            ServerHostnameParameter.validate(hostname, botProperties.allowLocalAddressRanges)
            serverStatusMonitor.hostname = hostname
        }
        if (queryPort != null) {
            serverStatusMonitor.queryPort = queryPort
        }
        if (apiHostname != null) {
            serverStatusMonitor.apiHostname = determineValueOfNullableStringParameter(apiHostname).also {
                ServerApiHostnameParameter.validate(it, botProperties.allowLocalAddressRanges)
            }
        }
        if (apiPort != null) {
            serverStatusMonitor.apiPort = if (apiPort == -1) null else apiPort
        }
        if (apiUsername != null) {
            serverStatusMonitor.apiUsername = determineValueOfNullableStringParameter(apiUsername)
        }
        if (apiPassword != null) {
            serverStatusMonitor.apiPassword = determineValueOfNullableStringParameter(apiPassword)
        }
        if (status != null) {
            serverStatusMonitor.status = status
        }
        if (embedEnabled != null) {
            serverStatusMonitor.embedEnabled = embedEnabled
        }
        if (displayServerDescription != null) {
            serverStatusMonitor.displayServerDescription = displayServerDescription
        }
        if (displayPlayerGearLevel != null) {
            serverStatusMonitor.displayPlayerGearLevel = displayPlayerGearLevel
        }
        if (playerActivityFeedChannelId != null) {
            serverStatusMonitor.playerActivityDiscordChannelId = determineValueOfNullableStringParameter(playerActivityFeedChannelId)
        }
        if (pvpKillFeedChannelId != null) {
            serverStatusMonitor.pvpKillFeedDiscordChannelId = determineValueOfNullableStringParameter(pvpKillFeedChannelId)
        }

        serverStatusMonitorRepository.updateServerStatusMonitor(serverStatusMonitor)

        logger.info("Successfully updated monitor with id '${serverStatusMonitorId}'.")

        interaction.deferEphemeralResponse().respond {
            content = "Updated server status monitor with id '${serverStatusMonitorId}'. It may take some time until the status message is updated."
        }
    }

    private fun determineValueOfNullableStringParameter(value: String): String? {
        return when (value) {
            "~" -> null
            else -> value
        }
    }
}
