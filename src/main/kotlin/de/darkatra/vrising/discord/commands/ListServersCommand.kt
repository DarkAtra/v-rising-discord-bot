package de.darkatra.vrising.discord.commands

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.commands.parameters.PageParameter
import de.darkatra.vrising.discord.commands.parameters.addPageParameter
import de.darkatra.vrising.discord.commands.parameters.getPageParameter
import de.darkatra.vrising.discord.persistence.ServerRepository
import de.darkatra.vrising.discord.persistence.model.Server
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
    private val serverRepository: ServerRepository
) : Command {

    private val name: String = "list-servers"
    private val description: String = "Lists all servers."

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
            is GuildChatInputCommandInteraction -> serverRepository.count(interaction.guildId.toString())
            is GlobalChatInputCommandInteraction -> serverRepository.count()
        }
        val totalPages = totalElements / PAGE_SIZE + 1

        if (page >= totalPages) {
            interaction.deferEphemeralResponse().respond {
                content = "This page does not exist."
            }
            return
        }

        val servers: List<Server> = when (interaction) {
            is GuildChatInputCommandInteraction -> serverRepository.getServers(
                discordServerId = interaction.guildId.toString(),
                offset = page * PAGE_SIZE,
                limit = PAGE_SIZE
            )

            is GlobalChatInputCommandInteraction -> serverRepository.getServers(
                offset = page * PAGE_SIZE,
                limit = PAGE_SIZE
            )
        }

        interaction.deferEphemeralResponse().respond {
            content = when (servers.isEmpty()) {
                true -> "No servers found."
                false -> servers.joinToString(separator = "\n") { server ->
                    "${server.id} - ${server.hostname}:${server.queryPort} - ${server.status.name}"
                } + "\n*Current Page: $page, Total Pages: $totalPages*"
            }
        }
    }
}
