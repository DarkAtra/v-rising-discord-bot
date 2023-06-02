package de.darkatra.vrising.discord.command

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.command.parameter.addServerStatusMonitorIdParameter
import de.darkatra.vrising.discord.command.parameter.getServerStatusMonitorIdParameter
import de.darkatra.vrising.discord.serverstatus.ServerStatusMonitorService
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.message.modify.embed
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils

@Component
class GetServerDetailsCommand(
    private val serverStatusMonitorService: ServerStatusMonitorService,
    private val botProperties: BotProperties
) : Command {

    private val name: String = "get-server-details"
    private val description: String = "Gets all the configuration details for the specified server."

    override fun getCommandName(): String = name

    override suspend fun register(kord: Kord) {

        kord.createGlobalChatInputCommand(
            name = name,
            description = description
        ) {
            dmPermission = false
            disableCommandInGuilds()

            addServerStatusMonitorIdParameter()
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val response = interaction.deferEphemeralResponse()

        val serverStatusMonitorId = interaction.getServerStatusMonitorIdParameter()
        val discordServerId = (interaction as GuildChatInputCommandInteraction).guildId

        val serverStatusMonitor = serverStatusMonitorService.getServerStatusMonitor(serverStatusMonitorId, discordServerId.toString())
        if (serverStatusMonitor == null) {
            response.respond {
                content = "No server with id '$serverStatusMonitorId' was found."
            }
            return
        }

        response.respond {
            embed {
                title = "Details for ${serverStatusMonitor.id}"

                field {
                    name = "Hostname"
                    value = serverStatusMonitor.hostName
                    inline = true
                }

                field {
                    name = "Query Port"
                    value = "${serverStatusMonitor.queryPort}"
                    inline = true
                }

                field {
                    name = "Api Port"
                    value = when (serverStatusMonitor.apiPort != null) {
                        true -> "${serverStatusMonitor.apiPort}"
                        false -> "-"
                    }
                    inline = true
                }

                field {
                    name = "Display Server Description"
                    value = "${serverStatusMonitor.displayServerDescription}"
                    inline = true
                }

                field {
                    name = "Status"
                    value = serverStatusMonitor.status.name
                    inline = true
                }

                field {
                    name = "Discord Server Id"
                    value = serverStatusMonitor.discordServerId
                    inline = true
                }

                field {
                    name = "Discord Channel Id"
                    value = serverStatusMonitor.discordChannelId
                    inline = true
                }

                field {
                    name = "Current Embed Message Id"
                    value = serverStatusMonitor.currentEmbedMessageId ?: "-"
                    inline = true
                }

                field {
                    name = "Current Failed Attempts"
                    value = "${serverStatusMonitor.currentFailedAttempts}"
                    inline = true
                }

                field {
                    name = "Most recent Errors"
                    value = when (serverStatusMonitor.recentErrors.isEmpty()) {
                        true -> "-"
                        false -> serverStatusMonitor.recentErrors.joinToString("\n") {
                            "${it.timestamp}```${StringUtils.truncate(it.message, botProperties.maxCharactersPerError)}```"
                        }
                    }
                }
            }
        }
    }
}
