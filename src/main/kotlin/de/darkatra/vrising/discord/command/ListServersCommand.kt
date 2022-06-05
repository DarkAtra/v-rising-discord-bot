package de.darkatra.vrising.discord.command

import de.darkatra.vrising.discord.ServerStatusMonitorService
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import org.springframework.stereotype.Component

@Component
class ListServersCommand(
    private val serverStatusMonitorService: ServerStatusMonitorService,
) : Command {

    private val name: String = "list-servers"
    private val description: String = "Lists all server status monitors."

    override fun getCommandName(): String = name

    override suspend fun register(kord: Kord) {

        kord.createGlobalChatInputCommand(
            name = name,
            description = description
        )
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val serverStatusConfigurations = serverStatusMonitorService.getServerStatusMonitors()

        val response = interaction.deferEphemeralResponse()
        response.respond {
            content = when (serverStatusConfigurations.isEmpty()) {
                true -> "No servers found."
                false -> serverStatusConfigurations.joinToString(separator = "\n") { serverStatusConfiguration ->
                    "${serverStatusConfiguration.id} - ${serverStatusConfiguration.hostName}:${serverStatusConfiguration.queryPort}"
                }
            }
        }
    }
}
