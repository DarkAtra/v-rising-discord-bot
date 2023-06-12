package de.darkatra.vrising.discord.command

import de.darkatra.vrising.discord.command.parameter.ServerApiHostnameParameter
import de.darkatra.vrising.discord.command.parameter.ServerHostnameParameter
import de.darkatra.vrising.discord.command.parameter.addDisplayPlayerGearLevelParameter
import de.darkatra.vrising.discord.command.parameter.addDisplayServerDescriptionParameter
import de.darkatra.vrising.discord.command.parameter.addPlayerActivityFeedChannelIdParameter
import de.darkatra.vrising.discord.command.parameter.addServerApiHostnameParameter
import de.darkatra.vrising.discord.command.parameter.addServerApiPortParameter
import de.darkatra.vrising.discord.command.parameter.addServerHostnameParameter
import de.darkatra.vrising.discord.command.parameter.addServerQueryPortParameter
import de.darkatra.vrising.discord.command.parameter.addServerStatusMonitorIdParameter
import de.darkatra.vrising.discord.command.parameter.addServerStatusMonitorStatusParameter
import de.darkatra.vrising.discord.command.parameter.getDisplayPlayerGearLevelParameter
import de.darkatra.vrising.discord.command.parameter.getDisplayServerDescriptionParameter
import de.darkatra.vrising.discord.command.parameter.getPlayerActivityFeedChannelIdParameter
import de.darkatra.vrising.discord.command.parameter.getServerApiHostnameParameter
import de.darkatra.vrising.discord.command.parameter.getServerApiPortParameter
import de.darkatra.vrising.discord.command.parameter.getServerHostnameParameter
import de.darkatra.vrising.discord.command.parameter.getServerQueryPortParameter
import de.darkatra.vrising.discord.command.parameter.getServerStatusMonitorIdParameter
import de.darkatra.vrising.discord.command.parameter.getServerStatusMonitorStatusParameter
import de.darkatra.vrising.discord.serverstatus.ServerStatusMonitorRepository
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import org.springframework.stereotype.Component

@Component
class UpdateServerCommand(
    private val serverStatusMonitorRepository: ServerStatusMonitorRepository,
) : Command {

    private val name: String = "update-server"
    private val description: String = "Updates the given server status monitor."

    override fun getCommandName(): String = name

    override suspend fun register(kord: Kord) {

        kord.createGlobalChatInputCommand(
            name = name,
            description = description
        ) {
            dmPermission = false
            disableCommandInGuilds()

            addServerStatusMonitorIdParameter()

            addServerHostnameParameter(required = false)
            addServerQueryPortParameter(required = false)
            addServerApiHostnameParameter(required = false)
            addServerApiPortParameter(required = false)
            addServerStatusMonitorStatusParameter(required = false)

            addDisplayServerDescriptionParameter(required = false)
            addDisplayPlayerGearLevelParameter(required = false)

            addPlayerActivityFeedChannelIdParameter(required = false)
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val serverStatusMonitorId = interaction.getServerStatusMonitorIdParameter()
        val hostname = interaction.getServerHostnameParameter()
        val queryPort = interaction.getServerQueryPortParameter()
        val apiHostname = interaction.getServerApiHostnameParameter()
        val apiPort = interaction.getServerApiPortParameter()
        val status = interaction.getServerStatusMonitorStatusParameter()

        val displayServerDescription = interaction.getDisplayServerDescriptionParameter()
        val displayPlayerGearLevel = interaction.getDisplayPlayerGearLevelParameter()

        val playerActivityFeedChannelId = interaction.getPlayerActivityFeedChannelIdParameter()

        val discordServerId = (interaction as GuildChatInputCommandInteraction).guildId

        val serverStatusMonitor = serverStatusMonitorRepository.getServerStatusMonitor(serverStatusMonitorId, discordServerId.toString())
        if (serverStatusMonitor == null) {
            interaction.deferEphemeralResponse().respond {
                content = "No server with id '$serverStatusMonitorId' was found."
            }
            return
        }

        if (hostname != null) {
            ServerHostnameParameter.validate(hostname)
            serverStatusMonitor.hostname = hostname
        }
        if (queryPort != null) {
            serverStatusMonitor.queryPort = queryPort
        }
        if (apiHostname != null) {
            if (apiHostname == "~") {
                serverStatusMonitor.apiHostname = null
            } else {
                ServerApiHostnameParameter.validate(apiHostname)
                serverStatusMonitor.apiHostname = apiHostname
            }
        }
        if (apiPort != null) {
            serverStatusMonitor.apiPort = if (apiPort == -1) null else apiPort
        }
        if (status != null) {
            serverStatusMonitor.status = status
        }
        if (displayServerDescription != null) {
            serverStatusMonitor.displayServerDescription = displayServerDescription
        }
        if (displayPlayerGearLevel != null) {
            serverStatusMonitor.displayPlayerGearLevel = displayPlayerGearLevel
        }
        if (playerActivityFeedChannelId != null) {
            if (apiHostname == "~") {
                serverStatusMonitor.playerActivityDiscordChannelId = null
            } else {
                serverStatusMonitor.playerActivityDiscordChannelId = playerActivityFeedChannelId
            }
        }

        serverStatusMonitorRepository.updateServerStatusMonitor(serverStatusMonitor)

        interaction.deferEphemeralResponse().respond {
            content = "Updated server status monitor with id '${serverStatusMonitorId}'. It may take some time until the status message is updated."
        }
    }
}
