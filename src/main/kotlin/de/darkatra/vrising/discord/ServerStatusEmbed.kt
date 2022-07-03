package de.darkatra.vrising.discord

import com.ibasco.agql.protocols.valve.source.query.info.SourceServer
import com.ibasco.agql.protocols.valve.source.query.players.SourcePlayer
import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder

object ServerStatusEmbed {

    fun buildEmbed(
        serverInfo: SourceServer,
        players: List<SourcePlayer>,
        rules: Map<String, Any>,
        displayServerDescription: Boolean,
        embedBuilder: EmbedBuilder
    ) {
        embedBuilder.apply {
            title = serverInfo.name
            color = Color(
                red = 0,
                green = 142,
                blue = 68
            )

            if (displayServerDescription) {
                val description = rules.filterKeys { key -> key.startsWith("desc") }
                    .mapKeys { (key, _) -> key.removePrefix("desc").toInt() }
                    .toList()
                    .sortedBy { (key, _) -> key }
                    .map { (_, value) -> value }
                    .joinToString(separator = "")
                    .trim()

                if (description.isNotBlank()) {
                    this.description = description
                }
            }

            field {
                name = "Ip and Port"
                value = "${serverInfo.hostAddress}:${serverInfo.gamePort}"
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

            if (players.isNotEmpty()) {
                players.sortedBy { player -> player.name }
                    .chunked(20)
                    .forEach { chunk ->
                        field {
                            name = "Online players"
                            value = chunk.joinToString(separator = "\n") { player -> "**${player.name}**" }
                            inline = true
                        }
                    }
            }
        }
    }
}
