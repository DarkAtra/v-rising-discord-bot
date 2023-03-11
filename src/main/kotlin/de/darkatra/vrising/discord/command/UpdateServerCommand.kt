package de.darkatra.vrising.discord.command

import de.darkatra.vrising.discord.ServerStatusMonitorService
import de.darkatra.vrising.discord.command.parameter.ServerHostnameParameter
import de.darkatra.vrising.discord.command.parameter.addDisplayServerDescriptionParameter
import de.darkatra.vrising.discord.command.parameter.addServerHostnameParameter
import de.darkatra.vrising.discord.command.parameter.addServerQueryPortParameter
import de.darkatra.vrising.discord.command.parameter.addServerStatusMonitorIdParameter
import de.darkatra.vrising.discord.command.parameter.addServerStatusMonitorStatusParameter
import de.darkatra.vrising.discord.command.parameter.getDisplayServerDescriptionParameter
import de.darkatra.vrising.discord.command.parameter.getServerHostnameParameter
import de.darkatra.vrising.discord.command.parameter.getServerQueryPortParameter
import de.darkatra.vrising.discord.command.parameter.getServerStatusMonitorIdParameter
import de.darkatra.vrising.discord.command.parameter.getServerStatusMonitorStatusParameter
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import org.springframework.stereotype.Component

@Component
class UpdateServerCommand(
    private val serverStatusMonitorService: ServerStatusMonitorService,
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
            addServerStatusMonitorStatusParameter(required = false)
            addDisplayServerDescriptionParameter(required = false)
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val serverStatusMonitorId = interaction.getServerStatusMonitorIdParameter()
        val hostName = interaction.getServerHostnameParameter()
        val queryPort = interaction.getServerQueryPortParameter()
        val status = interaction.getServerStatusMonitorStatusParameter()
        val displayServerDescription = interaction.getDisplayServerDescriptionParameter()

        val discordServerId = (interaction as GuildChatInputCommandInteraction).guildId

        val serverStatusMonitor = serverStatusMonitorService.getServerStatusMonitor(serverStatusMonitorId, discordServerId.toString())
        if (serverStatusMonitor == null) {
            interaction.deferEphemeralResponse().respond {
                content = "No server with id '$serverStatusMonitorId' was found."
            }
            return
        }

        val serverStatusMonitorBuilder = serverStatusMonitor.builder()
        if (hostName != null) {
            ServerHostnameParameter.validate(hostName)
            serverStatusMonitorBuilder.hostName = hostName
        }
        if (queryPort != null) {
            serverStatusMonitorBuilder.queryPort = queryPort
        }
        if (status != null) {
            serverStatusMonitorBuilder.status = status
        }
        if (displayServerDescription != null) {
            serverStatusMonitorBuilder.displayServerDescription = displayServerDescription
        }

        serverStatusMonitorService.putServerStatusMonitor(serverStatusMonitorBuilder.build())

        interaction.deferEphemeralResponse().respond {
            content = "Updated server status monitor with id '${serverStatusMonitorId}'. It may take some time until the status message is updated."
        }
    }
}
