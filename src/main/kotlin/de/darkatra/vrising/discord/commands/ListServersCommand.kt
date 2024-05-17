package de.darkatra.vrising.discord.commands

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.commands.parameters.PageParameter
import de.darkatra.vrising.discord.commands.parameters.addPageParameter
import de.darkatra.vrising.discord.commands.parameters.getPageParameter
import de.darkatra.vrising.discord.serverstatus.ServerStatusMonitorRepository
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GlobalChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

private const val PAGE_SIZE = 10

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

            addPageParameter(required = false)
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val page = interaction.getPageParameter() ?: 0
        PageParameter.validate(page)

        val totalElements = when (interaction) {
            is GuildChatInputCommandInteraction -> serverStatusMonitorRepository.count(interaction.guildId.toString())
            is GlobalChatInputCommandInteraction -> serverStatusMonitorRepository.count()
        }
        val totalPages = totalElements / PAGE_SIZE + 1

        if (page >= totalPages) {
            interaction.deferEphemeralResponse().respond {
                content = "This page does not exist."
            }
            return
        }

        val serverStatusMonitors: List<ServerStatusMonitor> = when (interaction) {
            is GuildChatInputCommandInteraction -> serverStatusMonitorRepository.getServerStatusMonitors(
                discordServerId = interaction.guildId.toString(),
                offset = page * PAGE_SIZE,
                limit = PAGE_SIZE
            )

            is GlobalChatInputCommandInteraction -> serverStatusMonitorRepository.getServerStatusMonitors(
                offset = page * PAGE_SIZE,
                limit = PAGE_SIZE
            )
        }

        interaction.deferEphemeralResponse().respond {
            content = when (serverStatusMonitors.isEmpty()) {
                true -> "No servers found."
                false -> serverStatusMonitors.joinToString(separator = "\n") { serverStatusMonitor ->
                    "${serverStatusMonitor.id} - ${serverStatusMonitor.hostname}:${serverStatusMonitor.queryPort} - ${serverStatusMonitor.status.name}"
                } + "\n*Current Page: $page, Total Pages: $totalPages*"
            }
        }
    }
}
