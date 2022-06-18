package de.darkatra.vrising.discord.command

import de.darkatra.vrising.discord.ServerStatusMonitorService
import de.darkatra.vrising.discord.command.parameter.addServerStatusMonitorIdParameter
import de.darkatra.vrising.discord.command.parameter.getServerStatusMonitorIdParameter
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
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
            addServerStatusMonitorIdParameter()
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val serverStatusMonitorId = interaction.getServerStatusMonitorIdParameter()
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
