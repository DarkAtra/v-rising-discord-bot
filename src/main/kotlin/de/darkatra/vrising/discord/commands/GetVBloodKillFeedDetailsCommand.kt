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
class GetVBloodKillFeedDetailsCommand(
    private val serverRepository: ServerRepository,
    private val botProperties: BotProperties
) : Command {

    private val name: String = "get-vblood-kill-feed-details"
    private val description: String = "Gets all details of the vblood kill feed for the specified server."

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

    @Suppress("DuplicatedCode")
    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val server = interaction.getServer(serverRepository)
            ?: return

        val vBloodKillFeed = server.vBloodKillFeed
        if (vBloodKillFeed == null) {
            interaction.deferEphemeralResponse().respond {
                content = "No vblood kill feed is configured for server with id '${server.id}'."
            }
            return
        }

        interaction.deferEphemeralResponse().respond {
            embed {
                title = "VBlood Kill Feed Details for ${server.id}"

                field {
                    name = "Status"
                    value = vBloodKillFeed.status.name
                    inline = true
                }

                field {
                    name = "Discord Server Id"
                    value = server.discordServerId
                    inline = true
                }

                field {
                    name = "Discord Channel Id"
                    value = vBloodKillFeed.discordChannelId
                    inline = true
                }

                field {
                    name = "Current Failed Attempts"
                    value = "${vBloodKillFeed.currentFailedAttempts}"
                    inline = true
                }

                field {
                    name = "Last Updated"
                    value = "<t:${vBloodKillFeed.lastUpdated.epochSecond}:R>"
                    inline = true
                }

                renderRecentErrors(vBloodKillFeed, botProperties.maxCharactersPerError)
            }
        }
    }
}
