package de.darkatra.vrising.discord.commands

import de.darkatra.vrising.discord.commands.parameters.addServerStatusMonitorIdParameter
import de.darkatra.vrising.discord.commands.parameters.getServerStatusMonitorIdParameter
import de.darkatra.vrising.discord.persistence.ServerStatusMonitorRepository
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GlobalChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class RemoveServerCommand(
    private val serverStatusMonitorRepository: ServerStatusMonitorRepository
) : Command {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val name: String = "remove-server"
    private val description: String = "Removes a server from the status monitor."

    override fun getCommandName(): String = name

    override suspend fun register(kord: Kord) {

        kord.createGlobalChatInputCommand(
            name = name,
            description = description
        ) {
            dmPermission = true
            disableCommandInGuilds()

            addServerStatusMonitorIdParameter()
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val serverStatusMonitorId = interaction.getServerStatusMonitorIdParameter()

        val wasSuccessful = when (interaction) {
            is GuildChatInputCommandInteraction -> serverStatusMonitorRepository.removeServerStatusMonitor(
                serverStatusMonitorId,
                interaction.guildId.toString()
            )

            is GlobalChatInputCommandInteraction -> serverStatusMonitorRepository.removeServerStatusMonitor(serverStatusMonitorId)
        }

        logger.info("Successfully removed monitor with id '${serverStatusMonitorId}'.")

        interaction.deferEphemeralResponse().respond {
            content = when (wasSuccessful) {
                true -> "Removed monitor with id '$serverStatusMonitorId'."
                false -> "No server with id '$serverStatusMonitorId' was found."
            }
        }
    }
}
