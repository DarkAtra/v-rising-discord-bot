package de.darkatra.vrising.discord.command

import de.darkatra.vrising.discord.command.parameter.ServerHostnameParameter
import de.darkatra.vrising.discord.command.parameter.addDisplayClanParameter
import de.darkatra.vrising.discord.command.parameter.addDisplayGearLevelParameter
import de.darkatra.vrising.discord.command.parameter.addDisplayKilledVBloodsParameter
import de.darkatra.vrising.discord.command.parameter.addDisplayPlayersAsAsciiTable
import de.darkatra.vrising.discord.command.parameter.addDisplayServerDescriptionParameter
import de.darkatra.vrising.discord.command.parameter.addServerApiPortParameter
import de.darkatra.vrising.discord.command.parameter.addServerHostnameParameter
import de.darkatra.vrising.discord.command.parameter.addServerQueryPortParameter
import de.darkatra.vrising.discord.command.parameter.addServerStatusMonitorIdParameter
import de.darkatra.vrising.discord.command.parameter.addServerStatusMonitorStatusParameter
import de.darkatra.vrising.discord.command.parameter.getDisplayClanParameter
import de.darkatra.vrising.discord.command.parameter.getDisplayGearLevelParameter
import de.darkatra.vrising.discord.command.parameter.getDisplayKilledVBloodsParameter
import de.darkatra.vrising.discord.command.parameter.getDisplayPlayersAsAsciiTable
import de.darkatra.vrising.discord.command.parameter.getDisplayServerDescriptionParameter
import de.darkatra.vrising.discord.command.parameter.getServerApiPortParameter
import de.darkatra.vrising.discord.command.parameter.getServerHostnameParameter
import de.darkatra.vrising.discord.command.parameter.getServerQueryPortParameter
import de.darkatra.vrising.discord.command.parameter.getServerStatusMonitorIdParameter
import de.darkatra.vrising.discord.command.parameter.getServerStatusMonitorStatusParameter
import de.darkatra.vrising.discord.serverstatus.ServerStatusMonitorService
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
            addServerApiPortParameter(required = false)
            addServerStatusMonitorStatusParameter(required = false)

            addDisplayPlayersAsAsciiTable(required = false)
            addDisplayServerDescriptionParameter(required = false)
            addDisplayClanParameter(required = false)
            addDisplayGearLevelParameter(required = false)
            addDisplayKilledVBloodsParameter(required = false)
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val serverStatusMonitorId = interaction.getServerStatusMonitorIdParameter()
        val hostName = interaction.getServerHostnameParameter()
        val queryPort = interaction.getServerQueryPortParameter()
        val apiPort = interaction.getServerApiPortParameter()
        val status = interaction.getServerStatusMonitorStatusParameter()

        val displayPlayersAsAsciiTable = interaction.getDisplayPlayersAsAsciiTable()
        val displayServerDescription = interaction.getDisplayServerDescriptionParameter()
        val displayClan = interaction.getDisplayClanParameter()
        val displayGearLevel = interaction.getDisplayGearLevelParameter()
        val displayKilledVBloods = interaction.getDisplayKilledVBloodsParameter()

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
        if (apiPort != null) {
            serverStatusMonitorBuilder.apiPort = if (apiPort == -1) null else apiPort
        }
        if (status != null) {
            serverStatusMonitorBuilder.status = status
        }
        if (displayPlayersAsAsciiTable != null) {
            serverStatusMonitorBuilder.displayPlayersAsAsciiTable = displayPlayersAsAsciiTable
        }
        if (displayServerDescription != null) {
            serverStatusMonitorBuilder.displayServerDescription = displayServerDescription
        }
        if (displayClan != null) {
            serverStatusMonitorBuilder.displayClan = displayClan
        }
        if (displayGearLevel != null) {
            serverStatusMonitorBuilder.displayGearLevel = displayGearLevel
        }
        if (displayKilledVBloods != null) {
            serverStatusMonitorBuilder.displayKilledVBloods = displayKilledVBloods
        }

        serverStatusMonitorService.putServerStatusMonitor(serverStatusMonitorBuilder.build())

        interaction.deferEphemeralResponse().respond {
            content = "Updated server status monitor with id '${serverStatusMonitorId}'. It may take some time until the status message is updated."
        }
    }
}
