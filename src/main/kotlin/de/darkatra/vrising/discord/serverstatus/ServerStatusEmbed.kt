package de.darkatra.vrising.discord.serverstatus

import com.github.freva.asciitable.AsciiTable
import de.darkatra.vrising.discord.serverstatus.model.Player
import de.darkatra.vrising.discord.serverstatus.model.ServerInfo
import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder
import java.lang.String.CASE_INSENSITIVE_ORDER

object ServerStatusEmbed {

    fun buildEmbed(
        serverInfo: ServerInfo,
        apiPortEnabled: Boolean,
        displayPlayersAsAsciiTable: Boolean,
        displayServerDescription: Boolean,
        displayClan: Boolean,
        displayGearLevel: Boolean,
        displayKilledVBloods: Boolean,
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
                value = "${serverInfo.numberOfPlayers}/${serverInfo.maxPlayers}"
                inline = true
            }

            // days-runningv2 -> for how many days the server has been running in real-time days (introduced in 0.5.42553)
            // days-running -> for how many days the server has been running in in-game days (pre 0.5.42553)
            val currentDay = serverInfo.rules["days-runningv2"]
            field {
                name = when (currentDay != null) {
                    true -> "Days running"
                    false -> "Ingame days"
                }
                // fallback to the old field for older servers and "-" if both fields are absent
                value = "${currentDay ?: serverInfo.rules["days-running"] ?: "-"}"
                inline = true
            }

            when (displayPlayersAsAsciiTable) {
                true -> buildAsciiPlayerList(serverInfo.players, apiPortEnabled, displayClan, displayGearLevel, displayKilledVBloods, embedBuilder)
                false -> buildPlainPlayerList(serverInfo.players, apiPortEnabled, displayClan, displayGearLevel, displayKilledVBloods, embedBuilder)
            }
        }
    }

    private fun buildPlainPlayerList(
        players: List<Player>,
        apiPortEnabled: Boolean,
        displayClan: Boolean,
        displayGearLevel: Boolean,
        displayKilledVBloods: Boolean,
        embedBuilder: EmbedBuilder
    ) {
        embedBuilder.apply {
            if (players.isNotEmpty()) {
                players.sortedWith(compareBy(CASE_INSENSITIVE_ORDER) { player -> player.name })
                    .chunked(20)
                    .forEach { chunk ->
                        field {
                            name = "Online players"
                            value = chunk.joinToString(separator = "\n") { player ->
                                buildList {
                                    add("**${player.name}**")
                                    if (apiPortEnabled) {
                                        if (displayClan) {
                                            add("Clan: ${player.clan ?: "-"}")
                                        }
                                        if (displayGearLevel) {
                                            add("Gear Level: ${player.gearLevel ?: 0}")
                                        }
                                        if (displayKilledVBloods) {
                                            add("VBloods: ${player.killedVBloods?.size ?: 0}")
                                        }
                                    }
                                }.joinToString(" | ")
                            }
                            inline = true
                        }
                    }
            }
        }
    }

    private fun buildAsciiPlayerList(
        players: List<Player>,
        apiPortEnabled: Boolean,
        displayClan: Boolean,
        displayGearLevel: Boolean,
        displayKilledVBloods: Boolean,
        embedBuilder: EmbedBuilder
    ) {
        val sortedPlayers = players.sortedWith(compareBy(CASE_INSENSITIVE_ORDER) { player -> player.name })
        embedBuilder.apply {
            if (players.isNotEmpty()) {
                field {
                    name = "Online players"
                    value = "```\n" + AsciiTable.getTable(
                        AsciiTable.BASIC_ASCII,
                        buildList {
                            add("Name")
                            if (apiPortEnabled) {
                                if (displayClan) {
                                    add("Clan")
                                }
                                if (displayGearLevel) {
                                    add("Gear Level")
                                }
                                if (displayKilledVBloods) {
                                    add("VBloods")
                                }
                            }
                        }.toTypedArray(),
                        emptyArray(),
                        sortedPlayers.map { player ->
                            buildList {
                                add(player.name)
                                if (apiPortEnabled) {
                                    if (displayClan) {
                                        add(player.clan ?: "-")
                                    }
                                    if (displayGearLevel) {
                                        add("${player.gearLevel ?: 0}")
                                    }
                                    if (displayKilledVBloods) {
                                        add("${player.killedVBloods?.size ?: 0}")
                                    }
                                }
                            }.toTypedArray()
                        }.toTypedArray()
                    ) + "\n```"
                }
            }
        }
    }
}
