package de.darkatra.vrising.discord

import com.ibasco.agql.protocols.valve.source.query.info.SourceServer
import com.ibasco.agql.protocols.valve.source.query.players.SourcePlayer
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.modify.embed

object ServerStatusEmbed {

    suspend fun create(
        serverInfo: SourceServer,
        players: List<SourcePlayer>,
        rules: Map<String, Any>,
        displayPlayerGearLevel: Boolean,
        channel: MessageChannelBehavior
    ): Snowflake {
        return channel.createEmbed {
            buildEmbed(serverInfo, players, rules, displayPlayerGearLevel, this)
        }.id
    }

    suspend fun update(
        serverInfo: SourceServer,
        players: List<SourcePlayer>,
        rules: Map<String, Any>,
        displayPlayerGearLevel: Boolean,
        message: Message
    ): Snowflake {
        return message.edit {
            embed {
                buildEmbed(serverInfo, players, rules, displayPlayerGearLevel, this)
            }
        }.id
    }

    private fun buildEmbed(
        serverInfo: SourceServer,
        players: List<SourcePlayer>,
        rules: Map<String, Any>,
        displayPlayerGearLevel: Boolean,
        embedBuilder: EmbedBuilder
    ) {
        embedBuilder.apply {
            title = serverInfo.name
            color = Color(
                red = 0,
                green = 142,
                blue = 68
            )

            rules["desc0"]?.let { serverDescription ->
                description = "$serverDescription"
            }

            field {
                name = "Ip and Port"
                value = "${serverInfo.hostAddress}:${serverInfo.port}"
                inline = true
            }

            field {
                name = "Online count"
                value = "${serverInfo.numOfPlayers}/${serverInfo.maxPlayers}"
                inline = true
            }

            rules["days-running"]?.let { currentDay ->
                field {
                    name = "Ingame days"
                    value = "$currentDay"
                    inline = true
                }
            }

            players.sortedBy { player -> player.name }
                .chunked(20)
                .forEach { chunk ->
                    field {
                        name = "Online players"
                        value = chunk.joinToString(separator = "\n") { player ->
                            when (displayPlayerGearLevel) {
                                true -> "**${player.name}** - ${player.score}"
                                false -> "**${player.name}**"
                            }
                        }
                        inline = true
                    }
                }
        }
    }
}
