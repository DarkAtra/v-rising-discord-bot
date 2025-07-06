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
class GetRaidFeedDetailsCommand(
    private val serverRepository: ServerRepository,
    private val botProperties: BotProperties
) : Command {

    private val name: String = "get-raid-feed-details"
    private val description: String = "Gets all details of the raid feed for the specified server."

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

        val raidFeed = server.raidFeed
        if (raidFeed == null) {
            interaction.deferEphemeralResponse().respond {
                content = "No raid feed is configured for server with id '${server.id}'."
            }
            return
        }

        interaction.deferEphemeralResponse().respond {
            embed {
                title = "Raid Feed Details for ${server.id}"

                field {
                    name = "Status"
                    value = raidFeed.status.name
                    inline = true
                }

                field {
                    name = "Discord Server Id"
                    value = server.discordServerId
                    inline = true
                }

                field {
                    name = "Discord Channel Id"
                    value = raidFeed.discordChannelId
                    inline = true
                }

                field {
                    name = "Display Player Gear Level"
                    value = "${raidFeed.displayPlayerGearLevel}"
                    inline = true
                }

                field {
                    name = "Current Failed Attempts"
                    value = "${raidFeed.currentFailedAttempts}"
                    inline = true
                }

                field {
                    name = "Last Updated"
                    value = "<t:${raidFeed.lastUpdated.epochSecond}:R>"
                    inline = true
                }

                renderRecentErrors(raidFeed, botProperties.maxCharactersPerError)
            }
        }
    }
}
