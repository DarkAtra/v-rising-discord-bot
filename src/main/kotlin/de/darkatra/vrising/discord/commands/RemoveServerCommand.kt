package de.darkatra.vrising.discord.commands

import de.darkatra.vrising.discord.commands.parameters.addServerIdParameter
import de.darkatra.vrising.discord.commands.parameters.getServerIdParameter
import de.darkatra.vrising.discord.persistence.ServerRepository
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GlobalChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class RemoveServerCommand(
    private val serverRepository: ServerRepository
) : Command {

    private val logger by lazy { LoggerFactory.getLogger(javaClass) }

    private val name: String = "remove-server"
    private val description: String = "Removes a server."

    override fun getCommandName(): String = name

    override suspend fun register(kord: Kord) {

        kord.createGlobalChatInputCommand(
            name = name,
            description = description
        ) {
            dmPermission = true
            disableCommandInGuilds()

            addServerIdParameter()
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val serverId = interaction.getServerIdParameter()

        val wasSuccessful = when (interaction) {
            is GuildChatInputCommandInteraction -> serverRepository.removeServer(serverId, interaction.guildId.toString())
            is GlobalChatInputCommandInteraction -> serverRepository.removeServer(serverId)
        }

        if (wasSuccessful) {
            logger.info("Successfully removed server with id '$serverId'.")
        }

        interaction.deferEphemeralResponse().respond {
            content = when (wasSuccessful) {
                true -> "Removed server with id '$serverId'."
                false -> "No server with id '$serverId' was found."
            }
        }
    }
}
