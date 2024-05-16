package de.darkatra.vrising.discord.commands

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.commands.parameters.addServerStatusMonitorIdParameter
import de.darkatra.vrising.discord.commands.parameters.getServerStatusMonitorIdParameter
import de.darkatra.vrising.discord.serverstatus.ServerStatusMonitorRepository
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GlobalChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.rest.builder.message.modify.embed
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils

@Component
@EnableConfigurationProperties(BotProperties::class)
class GetServerDetailsCommand(
    private val serverStatusMonitorRepository: ServerStatusMonitorRepository,
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
            dmPermission = true
            disableCommandInGuilds()

            addServerStatusMonitorIdParameter()
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val serverStatusMonitorId = interaction.getServerStatusMonitorIdParameter()

        val serverStatusMonitor = when (interaction) {
            is GuildChatInputCommandInteraction -> serverStatusMonitorRepository.getServerStatusMonitor(serverStatusMonitorId, interaction.guildId.toString())
            is GlobalChatInputCommandInteraction -> serverStatusMonitorRepository.getServerStatusMonitor(serverStatusMonitorId)
        }

        if (serverStatusMonitor == null) {
            interaction.deferEphemeralResponse().respond {
                content = "No server with id '$serverStatusMonitorId' was found."
            }
            return
        }

        interaction.deferEphemeralResponse().respond {
            embed {
                title = "Details for ${serverStatusMonitor.id}"

                field {
                    name = "Hostname"
                    value = serverStatusMonitor.hostname
                    inline = true
                }

                field {
                    name = "Query Port"
                    value = "${serverStatusMonitor.queryPort}"
                    inline = true
                }

                field {
                    name = "Status"
                    value = serverStatusMonitor.status.name
                    inline = true
                }

                field {
                    name = "Api Hostname"
                    value = when (serverStatusMonitor.apiHostname != null) {
                        true -> "${serverStatusMonitor.apiHostname}"
                        false -> "-"
                    }
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
                    name = "Embed Enabled"
                    value = "${serverStatusMonitor.embedEnabled}"
                    inline = true
                }

                field {
                    name = "Display Server Description"
                    value = "${serverStatusMonitor.displayServerDescription}"
                    inline = true
                }

                field {
                    name = "Display Player Gear Level"
                    value = "${serverStatusMonitor.displayPlayerGearLevel}"
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
                    name = "Player Activity Feed Channel Id"
                    value = serverStatusMonitor.playerActivityDiscordChannelId ?: "-"
                    inline = true
                }

                field {
                    name = "Pvp Kill Feed Channel Id"
                    value = serverStatusMonitor.pvpKillFeedDiscordChannelId ?: "-"
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
