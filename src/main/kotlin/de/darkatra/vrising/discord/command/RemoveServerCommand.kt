package de.darkatra.vrising.discord.command

import de.darkatra.vrising.discord.ServerStatusMonitorService
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.interaction.string
import org.springframework.stereotype.Component

@Component
class RemoveServerCommand(
    private val serverStatusMonitorService: ServerStatusMonitorService,
) : Command {

    private val name: String = "remove-server"
    private val description: String = "Removes a server from the status monitor."

    override fun getCommandName(): String = name

    override suspend fun register(kord: Kord) {

        kord.createGlobalChatInputCommand(
            name = name,
            description = description
        ) {

            string(
                name = "server-status-monitor-id",
                description = "The id of the server status monitor."
            ) {
                required = true
            }
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val command = interaction.command
        val serverStatusMonitorId = command.strings["server-status-monitor-id"]!!
        val discordServerId = (interaction as GuildChatInputCommandInteraction).guildId

        val wasSuccessful = serverStatusMonitorService.removeServerStatusMonitor(serverStatusMonitorId, discordServerId.toString())

        interaction.deferEphemeralResponse().respond {
            content = when (wasSuccessful) {
                true -> "Removed monitor with id '$serverStatusMonitorId'."
                false -> "No server with id '$serverStatusMonitorId' was found."
            }
        }
    }
}
