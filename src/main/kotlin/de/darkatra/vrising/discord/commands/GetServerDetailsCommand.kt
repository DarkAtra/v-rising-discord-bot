package de.darkatra.vrising.discord.commands

import de.darkatra.vrising.discord.commands.parameters.addServerIdParameter
import de.darkatra.vrising.discord.persistence.ServerRepository
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.message.embed
import org.springframework.stereotype.Component
import java.util.Objects
import java.util.stream.Stream

@Component
class GetServerDetailsCommand(
    private val serverRepository: ServerRepository
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

            addServerIdParameter()
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val server = interaction.getServer(serverRepository)
            ?: return

        interaction.deferEphemeralResponse().respond {
            embed {
                title = "Details for ${server.id}"

                field {
                    name = "Hostname"
                    value = server.hostname
                    inline = true
                }

                field {
                    name = "Query Port"
                    value = "${server.queryPort}"
                    inline = true
                }

                field {
                    name = "Discord Server Id"
                    value = server.discordServerId
                    inline = true
                }

                field {
                    name = "Api Hostname"
                    value = when (server.apiHostname != null) {
                        true -> "${server.apiHostname}"
                        false -> "-"
                    }
                    inline = true
                }

                field {
                    name = "Api Port"
                    value = when (server.apiPort != null) {
                        true -> "${server.apiPort}"
                        false -> "-"
                    }
                    inline = true
                }

                field {
                    name = "Last Update Attempt"
                    value = "<t:${server.lastUpdated.epochSecond}:R>"
                    inline = true
                }

                field {
                    name = "Status Monitor Status"
                    value = server.statusMonitor?.status?.name ?: "-"
                    inline = true
                }

                field {
                    name = "Player Activity Feed Status"
                    value = server.playerActivityFeed?.status?.name ?: "-"
                    inline = true
                }

                field {
                    name = "Pvp Kill Feed Status"
                    value = server.pvpKillFeed?.status?.name ?: "-"
                    inline = true
                }

                field {
                    name = "Number of Leaderboards"
                    value = "${Stream.of(server.pvpLeaderboard).filter(Objects::nonNull).count()}"
                    inline = true
                }
            }
        }
    }
}
