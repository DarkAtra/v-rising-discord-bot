package de.darkatra.vrising.discord.commands

import de.darkatra.vrising.discord.commands.parameters.ChannelIdParameter
import de.darkatra.vrising.discord.commands.parameters.addChannelIdParameter
import de.darkatra.vrising.discord.commands.parameters.addDisplayPlayerGearLevelParameter
import de.darkatra.vrising.discord.commands.parameters.addServerIdParameter
import de.darkatra.vrising.discord.commands.parameters.addStatusParameter
import de.darkatra.vrising.discord.commands.parameters.getChannelIdParameter
import de.darkatra.vrising.discord.commands.parameters.getDisplayPlayerGearLevelParameter
import de.darkatra.vrising.discord.commands.parameters.getServerIdParameter
import de.darkatra.vrising.discord.commands.parameters.getStatusParameter
import de.darkatra.vrising.discord.persistence.ServerRepository
import de.darkatra.vrising.discord.persistence.model.RaidFeed
import de.darkatra.vrising.discord.persistence.model.Status
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GlobalChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ConfigureRaidFeedCommand(
    private val serverRepository: ServerRepository
) : Command {

    private val logger by lazy { LoggerFactory.getLogger(javaClass) }

    private val name: String = "configure-raid-feed"
    private val description: String = "Configures the raid feed for a given server."

    override fun getCommandName(): String = name

    override suspend fun register(kord: Kord) {

        kord.createGlobalChatInputCommand(
            name = name,
            description = description
        ) {

            dmPermission = true
            disableCommandInGuilds()

            addServerIdParameter()

            addChannelIdParameter(required = false)
            addStatusParameter(required = false)

            addDisplayPlayerGearLevelParameter(required = false)
        }
    }

    @Suppress("DuplicatedCode")
    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val serverId = interaction.getServerIdParameter()

        val channelId = interaction.getChannelIdParameter()
        val status = interaction.getStatusParameter()

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

        val raidFeed = server.raidFeed
        if (raidFeed == null) {

            if (channelId == null) {
                interaction.deferEphemeralResponse().respond {
                    content = "'${ChannelIdParameter.NAME}' is required when using this command for the first time."
                }
                return
            }

            server.raidFeed = RaidFeed(
                status = status ?: Status.ACTIVE,
                discordChannelId = channelId,
                displayPlayerGearLevel = displayPlayerGearLevel ?: false,
                lastUpdated = server.lastUpdated
            )

            serverRepository.updateServer(server)

            logger.info("Successfully configured the raid feed for server '$serverId'.")

            interaction.deferEphemeralResponse().respond {
                content = "Successfully configured the raid feed for server with id '$serverId'."
            }
            return
        }

        if (channelId != null) {
            raidFeed.discordChannelId = channelId
        }
        if (status != null) {
            raidFeed.status = status
        }
        if (displayPlayerGearLevel != null) {
            raidFeed.displayPlayerGearLevel = displayPlayerGearLevel
        }

        serverRepository.updateServer(server)

        logger.info("Successfully updated the raid feed for server '$serverId'.")

        interaction.deferEphemeralResponse().respond {
            content = "Successfully updated the raid feed for server with id '$serverId'."
        }
    }
}
