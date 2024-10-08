package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.serverstatus.model.ServerInfo
import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.datetime.toKotlinInstant
import java.lang.String.CASE_INSENSITIVE_ORDER
import java.time.Instant

object ServerStatusEmbed {

    fun buildEmbed(
        serverInfo: ServerInfo,
        apiEnabled: Boolean,
        displayServerDescription: Boolean,
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

            if (displayServerDescription) {
                val description = serverInfo.rules.filterKeys { key -> key.startsWith("desc") }
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
                value = "${serverInfo.ip}:${serverInfo.gamePort}"
                inline = true
            }

            field {
                name = "Online count"
                value = "${serverInfo.players.size}/${serverInfo.maxPlayers}"
                inline = true
            }

            // days-runningv2 -> for how many days the server has been running in real-time days (introduced in 0.5.42553)
            // days-running -> for how many days the server has been running in in-game days (pre 0.5.42553)
            val currentDay = serverInfo.rules["days-runningv2"]
            field {
                name = when {
                    currentDay != null -> "Days running"
                    else -> "Ingame days"
                }
                // fallback to the old field for older servers and "-" if both fields are absent
                value = "${currentDay ?: serverInfo.rules["days-running"] ?: "-"}"
                inline = true
            }

            if (serverInfo.players.isNotEmpty()) {
                serverInfo.players.sortedWith(compareBy(CASE_INSENSITIVE_ORDER) { player -> player.name })
                    .chunked(20)
                    .forEach { chunk ->
                        field {
                            name = "Online players"
                            value = chunk.joinToString(separator = "\n") { player ->
                                buildString {
                                    append("**${player.name}**")
                                    if (apiEnabled && displayPlayerGearLevel) {
                                        append(" - ${player.gearLevel ?: 0}")
                                    }
                                }
                            }
                            inline = true
                        }
                    }
            }

            timestamp = Instant.now().toKotlinInstant()
        }
    }
}
