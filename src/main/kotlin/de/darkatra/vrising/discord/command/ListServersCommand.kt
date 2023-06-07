package de.darkatra.vrising.discord.command

import de.darkatra.vrising.discord.serverstatus.ServerStatusMonitorRepository
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import org.springframework.stereotype.Component

@Component
class ListServersCommand(
    private val serverStatusMonitorRepository: ServerStatusMonitorRepository,
) : Command {

    private val name: String = "list-servers"
    private val description: String = "Lists all server status monitors."

    override fun getCommandName(): String = name

    override suspend fun register(kord: Kord) {

        kord.createGlobalChatInputCommand(
            name = name,
            description = description
        ) {
            dmPermission = false
            disableCommandInGuilds()
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val discordServerId = (interaction as GuildChatInputCommandInteraction).guildId

        val serverStatusConfigurations = serverStatusMonitorRepository.getServerStatusMonitors(discordServerId.toString())

        interaction.deferEphemeralResponse().respond {
            content = when (serverStatusConfigurations.isEmpty()) {
                true -> "No servers found."
                false -> serverStatusConfigurations.joinToString(separator = "\n") { serverStatusConfiguration ->
                    "${serverStatusConfiguration.id} - ${serverStatusConfiguration.hostname}:${serverStatusConfiguration.queryPort} - ${serverStatusConfiguration.status.name}"
                }
            }
        }
    }
}
