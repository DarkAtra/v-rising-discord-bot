package de.darkatra.vrising.discord.commands

import de.darkatra.vrising.discord.commands.parameters.ChannelIdParameter
import de.darkatra.vrising.discord.commands.parameters.addChannelIdParameter
import de.darkatra.vrising.discord.commands.parameters.addServerIdParameter
import de.darkatra.vrising.discord.commands.parameters.addStatusParameter
import de.darkatra.vrising.discord.commands.parameters.getChannelIdParameter
import de.darkatra.vrising.discord.commands.parameters.getServerIdParameter
import de.darkatra.vrising.discord.commands.parameters.getStatusParameter
import de.darkatra.vrising.discord.persistence.ServerRepository
import de.darkatra.vrising.discord.persistence.model.PlayerActivityFeed
import de.darkatra.vrising.discord.persistence.model.Status
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GlobalChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ConfigurePlayerActivityFeedCommand(
    private val serverRepository: ServerRepository
) : Command {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val name: String = "configure-player-activity-feed"
    private val description: String = "Configures the player activity feed for a given server."

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
        }
    }

    override fun isSupported(interaction: ChatInputCommandInteraction, adminUserIds: Set<String>): Boolean {
        if (interaction is GlobalChatInputCommandInteraction) {
            return false
        }
        return super.isSupported(interaction, adminUserIds)
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val serverId = interaction.getServerIdParameter()

        val channelId = interaction.getChannelIdParameter()
        val status = interaction.getStatusParameter()

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

        val playerActivityFeed = server.playerActivityFeed
        if (playerActivityFeed == null) {

            if (channelId == null) {
                interaction.deferEphemeralResponse().respond {
                    content = "'${ChannelIdParameter.NAME}' is required when using this command for the first time."
                }
                return
            }

            server.playerActivityFeed = PlayerActivityFeed(
                status = status ?: Status.ACTIVE,
                discordChannelId = channelId
            )

            logger.info("Successfully configured the player activity feed for server '$serverId'.")

            interaction.deferEphemeralResponse().respond {
                content = "Successfully configured the player activity feed for server with id '$serverId'."
            }
            return
        }

        if (channelId != null) {
            playerActivityFeed.discordChannelId = channelId
        }
        if (status != null) {
            playerActivityFeed.status = status
        }

        serverRepository.updateServer(server)

        logger.info("Successfully updated the player activity feed for server '$serverId'.")

        interaction.deferEphemeralResponse().respond {
            content = "Successfully updated the player activity feed for server with id '$serverId'."
        }
    }
}
