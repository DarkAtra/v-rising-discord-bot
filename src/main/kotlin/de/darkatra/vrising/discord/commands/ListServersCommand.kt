package de.darkatra.vrising.discord.commands

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.serverstatus.ServerStatusMonitorRepository
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GlobalChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

@Component
@EnableConfigurationProperties(BotProperties::class)
class ListServersCommand(
    private val serverStatusMonitorRepository: ServerStatusMonitorRepository
) : Command {

    private val name: String = "list-servers"
    private val description: String = "Lists all server status monitors."

    override fun getCommandName(): String = name

    override suspend fun register(kord: Kord) {

        kord.createGlobalChatInputCommand(
            name = name,
            description = description
        ) {
            dmPermission = true
            disableCommandInGuilds()
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val serverStatusMonitors: List<ServerStatusMonitor> = when (interaction) {
            is GuildChatInputCommandInteraction -> serverStatusMonitorRepository.getServerStatusMonitors(interaction.guildId.toString())
            is GlobalChatInputCommandInteraction -> serverStatusMonitorRepository.getServerStatusMonitors()
        }

        interaction.deferEphemeralResponse().respond {
            content = when (serverStatusMonitors.isEmpty()) {
                true -> "No servers found."
                false -> serverStatusMonitors.joinToString(separator = "\n") { serverStatusMonitor ->
                    "${serverStatusMonitor.id} - ${serverStatusMonitor.hostname}:${serverStatusMonitor.queryPort} - ${serverStatusMonitor.status.name}"
                }
            }
        }
    }
}
