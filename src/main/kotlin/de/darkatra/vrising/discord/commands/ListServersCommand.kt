package de.darkatra.vrising.discord.commands

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.commands.parameters.PageParameter
import de.darkatra.vrising.discord.commands.parameters.addDiscordServerIdParameter
import de.darkatra.vrising.discord.commands.parameters.addPageParameter
import de.darkatra.vrising.discord.commands.parameters.getDiscordServerIdParameter
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

private const val PAGE_SIZE = 10L

@Component
@EnableConfigurationProperties(BotProperties::class)
class ListServersCommand(
    private val serverRepository: ServerRepository
) : Command {

    private val name: String = "list-servers"
    private val description: String = "Lists all servers."

    override fun getCommandName(): String = name
    override fun getArgumentCount(): Int = 2

    override fun isSupported(interaction: ChatInputCommandInteraction, adminUserIds: Set<String>): Boolean {

        // DiscordServerIdParameter is only allowed in private messages by admins
        if (interaction is GuildChatInputCommandInteraction && interaction.getDiscordServerIdParameter() != null) {
            return false
        }

        return super.isSupported(interaction, adminUserIds)
    }

    override suspend fun register(kord: Kord) {

        kord.createGlobalChatInputCommand(
            name = name,
            description = description
        ) {
            dmPermission = true
            disableCommandInGuilds()

            addPageParameter(required = false)
            addDiscordServerIdParameter(required = false)
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val page = interaction.getPageParameter() ?: 0
        PageParameter.validate(page)

        val discordServerId = when (interaction) {
            is GuildChatInputCommandInteraction -> interaction.guildId.toString()
            is GlobalChatInputCommandInteraction -> interaction.getDiscordServerIdParameter()
        }

        val totalElements = serverRepository.count(discordServerId)
        val totalPages = totalElements / PAGE_SIZE + 1

        if (page >= totalPages) {
            interaction.deferEphemeralResponse().respond {
                content = "This page does not exist."
            }
            return
        }

        val servers: List<Server> = serverRepository.getServers(
            discordServerId = discordServerId.toString(),
            offset = page * PAGE_SIZE,
            limit = PAGE_SIZE
        )

        interaction.deferEphemeralResponse().respond {
            content = when {
                servers.isEmpty() -> "No servers found."
                else -> servers.joinToString(separator = "\n") { server ->
                    "${server.id} - ${server.hostname}:${server.queryPort} - ${server.status.name}"
                } + "\n*Current Page: $page, Total Pages: $totalPages*"
            }
        }
    }
}
