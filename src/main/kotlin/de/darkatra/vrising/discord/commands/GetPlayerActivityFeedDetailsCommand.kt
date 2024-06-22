package de.darkatra.vrising.discord.commands

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.commands.parameters.addServerIdParameter
import de.darkatra.vrising.discord.persistence.ServerRepository
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.message.embed
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

@Component
@EnableConfigurationProperties(BotProperties::class)
class GetPlayerActivityFeedDetailsCommand(
    private val serverRepository: ServerRepository,
    private val botProperties: BotProperties
) : Command {

    private val name: String = "get-player-activity-feed-details"
    private val description: String = "Gets all details of the player activity feed for the specified server."

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

        val server = interaction.getServer(serverRepository)
            ?: return

        val playerActivityFeed = server.playerActivityFeed
        if (playerActivityFeed == null) {
            interaction.deferEphemeralResponse().respond {
                content = "No player activity feed is configured for server with id '${server.id}'."
            }
            return
        }

        interaction.deferEphemeralResponse().respond {
            embed {
                title = "Player Activity Feed Details for ${server.id}"

                field {
                    name = "Status"
                    value = playerActivityFeed.status.name
                    inline = true
                }

                field {
                    name = "Discord Server Id"
                    value = server.discordServerId
                    inline = true
                }

                field {
                    name = "Discord Channel Id"
                    value = playerActivityFeed.discordChannelId
                    inline = true
                }

                field {
                    name = "Current Failed Attempts"
                    value = "${playerActivityFeed.currentFailedAttempts}"
                    inline = true
                }

                field {
                    name = "Last Updated"
                    value = "<t:${playerActivityFeed.getLastUpdated().epochSecond}:R>"
                    inline = true
                }

                renderRecentErrors(playerActivityFeed, botProperties.maxCharactersPerError)
            }
        }
    }
}
