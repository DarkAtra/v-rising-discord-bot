package de.darkatra.vrising.discord.commands

import de.darkatra.vrising.discord.commands.parameters.ChannelIdParameter
import de.darkatra.vrising.discord.commands.parameters.addChannelIdParameter
import de.darkatra.vrising.discord.commands.parameters.addDisplayPlayerGearLevelParameter
import de.darkatra.vrising.discord.commands.parameters.addDisplayServerDescriptionParameter
import de.darkatra.vrising.discord.commands.parameters.addServerIdParameter
import de.darkatra.vrising.discord.commands.parameters.addStatusParameter
import de.darkatra.vrising.discord.commands.parameters.getChannelIdParameter
import de.darkatra.vrising.discord.commands.parameters.getDisplayPlayerGearLevelParameter
import de.darkatra.vrising.discord.commands.parameters.getDisplayServerDescriptionParameter
import de.darkatra.vrising.discord.commands.parameters.getServerIdParameter
import de.darkatra.vrising.discord.commands.parameters.getStatusParameter
import de.darkatra.vrising.discord.persistence.ServerRepository
import de.darkatra.vrising.discord.persistence.model.Status
import de.darkatra.vrising.discord.persistence.model.StatusMonitor
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GlobalChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ConfigureStatusMonitorCommand(
    private val serverRepository: ServerRepository
) : Command {

    private val logger by lazy { LoggerFactory.getLogger(javaClass) }

    private val name: String = "configure-status-monitor"
    private val description: String = "Configures the status monitor for a given server."

    override fun getCommandName(): String = name

    override suspend fun register(kord: Kord) {

        kord.createGlobalChatInputCommand(
            name = name,
            description = description
        ) {

            dmPermission = false
            disableCommandInGuilds()

            addServerIdParameter()

            addChannelIdParameter(required = false)
            addStatusParameter(required = false)

            addDisplayServerDescriptionParameter(required = false)
            addDisplayPlayerGearLevelParameter(required = false)
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val serverId = interaction.getServerIdParameter()

        val channelId = interaction.getChannelIdParameter()
        val status = interaction.getStatusParameter()

        val displayServerDescription = interaction.getDisplayServerDescriptionParameter()
        val displayPlayerGearLevel = interaction.getDisplayPlayerGearLevelParameter()

        val server = when (interaction) {
            is GuildChatInputCommandInteraction -> serverRepository.getServer(serverId, interaction.guildId.toString())
            is GlobalChatInputCommandInteraction -> serverRepository.getServer(serverId)
        }

        if (server == null) {
            interaction.deferEphemeralResponse().respond {
                content = "No server with id '$serverId' was found."
            }
            return
        }

        val statusMonitor = server.statusMonitor
        if (statusMonitor == null) {

            if (channelId == null) {
                interaction.deferEphemeralResponse().respond {
                    content = "'${ChannelIdParameter.NAME}' is required when using this command for the first time."
                }
                return
            }

            server.statusMonitor = StatusMonitor(
                status = status ?: Status.ACTIVE,
                discordChannelId = channelId,
                displayServerDescription = displayServerDescription ?: true,
                displayPlayerGearLevel = displayPlayerGearLevel ?: true
            )

            serverRepository.updateServer(server)

            logger.info("Successfully configured the status monitor for server '$serverId'.")

            interaction.deferEphemeralResponse().respond {
                content = """Successfully configured the status monitor for server with id '$serverId'.
                    |It may take a few minutes for the status embed to appear.""".trimMargin()
            }
            return
        }

        if (channelId != null) {
            statusMonitor.discordChannelId = channelId
        }
        if (status != null) {
            statusMonitor.status = status
        }
        if (displayServerDescription != null) {
            statusMonitor.displayServerDescription = displayServerDescription
        }
        if (displayPlayerGearLevel != null) {
            statusMonitor.displayPlayerGearLevel = displayPlayerGearLevel
        }

        serverRepository.updateServer(server)

        logger.info("Successfully updated the status monitor for server '$serverId'.")

        interaction.deferEphemeralResponse().respond {
            content = """Successfully updated the status monitor for server with id '$serverId'.
                |It may take a few minutes for the status embed to reflect the changes.""".trimMargin()
        }
    }
}
