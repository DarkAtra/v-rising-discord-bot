package de.darkatra.vrising.discord.commands

import de.darkatra.vrising.discord.commands.parameters.ChannelIdParameter
import de.darkatra.vrising.discord.commands.parameters.addChannelIdParameter
import de.darkatra.vrising.discord.commands.parameters.addServerIdParameter
import de.darkatra.vrising.discord.commands.parameters.addStatusParameter
import de.darkatra.vrising.discord.commands.parameters.getChannelIdParameter
import de.darkatra.vrising.discord.commands.parameters.getServerIdParameter
import de.darkatra.vrising.discord.commands.parameters.getStatusParameter
import de.darkatra.vrising.discord.persistence.ServerRepository
import de.darkatra.vrising.discord.persistence.model.Status
import de.darkatra.vrising.discord.persistence.model.VBloodKillFeed
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GlobalChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ConfigureVBloodKillFeedCommand(
    private val serverRepository: ServerRepository
) : Command {

    private val logger by lazy { LoggerFactory.getLogger(javaClass) }

    private val name: String = "configure-vblood-kill-feed"
    private val description: String = "Configures the vblood kill feed for a given server."

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
        }
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

        val vBloodKillFeed = server.vBloodKillFeed
        if (vBloodKillFeed == null) {

            if (channelId == null) {
                interaction.deferEphemeralResponse().respond {
                    content = "'${ChannelIdParameter.NAME}' is required when using this command for the first time."
                }
                return
            }

            server.vBloodKillFeed = VBloodKillFeed(
                status = status ?: Status.ACTIVE,
                discordChannelId = channelId,
                lastUpdated = server.lastUpdated
            )

            serverRepository.updateServer(server)

            logger.info("Successfully configured the vblood kill feed for server '$serverId'.")

            interaction.deferEphemeralResponse().respond {
                content = "Successfully configured the vblood kill feed for server with id '$serverId'."
            }
            return
        }

        if (channelId != null) {
            vBloodKillFeed.discordChannelId = channelId
        }
        if (status != null) {
            vBloodKillFeed.status = status
        }

        serverRepository.updateServer(server)

        logger.info("Successfully updated the vblood kill feed for server '$serverId'.")

        interaction.deferEphemeralResponse().respond {
            content = "Successfully updated the vblood kill feed for server with id '$serverId'."
        }
    }
}
