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
class GetStatusMonitorDetailsCommand(
    private val serverRepository: ServerRepository,
    private val botProperties: BotProperties
) : Command {

    private val name: String = "get-status-monitor-details"
    private val description: String = "Gets all details of the status monitor for the specified server."

    override fun getCommandName(): String = name
    override fun getArgumentCount(): Int = 1

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

        val statusMonitor = server.statusMonitor
        if (statusMonitor == null) {
            interaction.deferEphemeralResponse().respond {
                content = "No status monitor is configured for server with id '${server.id}'."
            }
            return
        }

        interaction.deferEphemeralResponse().respond {
            embed {
                title = "Status Monitor Details for ${server.id}"

                field {
                    name = "Status"
                    value = statusMonitor.status.name
                    inline = true
                }

                field {
                    name = "Discord Server Id"
                    value = server.discordServerId
                    inline = true
                }

                field {
                    name = "Discord Channel Id"
                    value = statusMonitor.discordChannelId
                    inline = true
                }

                field {
                    name = "Display Server Description"
                    value = "${statusMonitor.displayServerDescription}"
                    inline = true
                }

                field {
                    name = "Display Player Gear Level"
                    value = "${statusMonitor.displayPlayerGearLevel}"
                    inline = true
                }

                field {
                    name = "Current Embed Message Id"
                    value = statusMonitor.currentEmbedMessageId ?: "-"
                    inline = true
                }

                field {
                    name = "Current Failed Attempts"
                    value = "${statusMonitor.currentFailedAttempts}"
                    inline = true
                }

                field {
                    name = "Current Failed Api Attempts"
                    value = "${statusMonitor.currentFailedApiAttempts}"
                    inline = true
                }

                field {
                    name = "Last Updated"
                    value = "<t:${server.lastUpdated.epochSecond}:R>"
                    inline = true
                }

                renderRecentErrors(statusMonitor, botProperties.maxCharactersPerError)
            }
        }
    }
}
