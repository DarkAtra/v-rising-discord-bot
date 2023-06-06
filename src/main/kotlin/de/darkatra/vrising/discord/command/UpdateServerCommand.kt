package de.darkatra.vrising.discord.command

import de.darkatra.vrising.discord.command.parameter.ServerApiHostnameParameter
import de.darkatra.vrising.discord.command.parameter.ServerHostnameParameter
import de.darkatra.vrising.discord.command.parameter.addDisplayPlayerGearLevelParameter
import de.darkatra.vrising.discord.command.parameter.addDisplayServerDescriptionParameter
import de.darkatra.vrising.discord.command.parameter.addServerApiPortParameter
import de.darkatra.vrising.discord.command.parameter.addServerHostnameParameter
import de.darkatra.vrising.discord.command.parameter.addServerQueryPortParameter
import de.darkatra.vrising.discord.command.parameter.addServerStatusMonitorIdParameter
import de.darkatra.vrising.discord.command.parameter.addServerStatusMonitorStatusParameter
import de.darkatra.vrising.discord.command.parameter.getDisplayPlayerGearLevelParameter
import de.darkatra.vrising.discord.command.parameter.getDisplayServerDescriptionParameter
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
            addServerApiPortParameter(required = false)
            addServerStatusMonitorStatusParameter(required = false)

            addDisplayServerDescriptionParameter(required = false)
            addDisplayPlayerGearLevelParameter(required = false)
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

        val discordServerId = (interaction as GuildChatInputCommandInteraction).guildId

        val serverStatusMonitor = serverStatusMonitorRepository.getServerStatusMonitor(serverStatusMonitorId, discordServerId.toString())
        if (serverStatusMonitor == null) {
            interaction.deferEphemeralResponse().respond {
                content = "No server with id '$serverStatusMonitorId' was found."
            }
            return
        }

        val serverStatusMonitorBuilder = serverStatusMonitor.builder()
        if (hostname != null) {
            ServerHostnameParameter.validate(hostname)
            serverStatusMonitorBuilder.hostname = hostname
        }
        if (queryPort != null) {
            serverStatusMonitorBuilder.queryPort = queryPort
        }
        if (apiHostname != null) {
            if (apiHostname == "~") {
                serverStatusMonitorBuilder.apiHostname = null
            } else {
                ServerApiHostnameParameter.validate(apiHostname)
                serverStatusMonitorBuilder.apiHostname = apiHostname
            }
        }
        if (apiPort != null) {
            serverStatusMonitorBuilder.apiPort = if (apiPort == -1) null else apiPort
        }
        if (status != null) {
            serverStatusMonitorBuilder.status = status
        }
        if (displayServerDescription != null) {
            serverStatusMonitorBuilder.displayServerDescription = displayServerDescription
        }
        if (displayPlayerGearLevel != null) {
            serverStatusMonitorBuilder.displayPlayerGearLevel = displayPlayerGearLevel
        }

        serverStatusMonitorRepository.putServerStatusMonitor(serverStatusMonitorBuilder.build())

        interaction.deferEphemeralResponse().respond {
            content = "Updated server status monitor with id '${serverStatusMonitorId}'. It may take some time until the status message is updated."
        }
    }
}
