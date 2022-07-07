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

            // days-runningv2 -> for how many days the server has been running in real-time days (introduced in 0.5.42553)
            // days-running -> for how many days the server has been running in in-game days (pre 0.5.42553)
            val currentDay = rules["days-runningv2"]
            field {
                name = when (currentDay != null) {
                    true -> "Days running"
                    false -> "Ingame days"
                }
                // fallback to the old field for older servers and "-" if both fields are absent
                value = "${currentDay ?: rules["days-running"] ?: "-"}"
                inline = true
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
