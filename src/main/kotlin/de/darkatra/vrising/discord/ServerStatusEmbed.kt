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

	suspend fun create(serverInfo: SourceServer, players: List<SourcePlayer>, rules: Map<String, Any>, channel: MessageChannelBehavior): Snowflake {
		return channel.createEmbed {
			buildEmbed(serverInfo, players, rules, this)
		}.id
	}

	suspend fun update(serverInfo: SourceServer, players: List<SourcePlayer>, rules: Map<String, Any>, message: Message): Snowflake {
		return message.edit {
			embed {
				buildEmbed(serverInfo, players, rules, this)
			}
		}.id
	}

	private fun buildEmbed(serverInfo: SourceServer, players: List<SourcePlayer>, rules: Map<String, Any>, embedBuilder: EmbedBuilder) {
		embedBuilder.apply {
			title = "Server Status"
			color = Color(
				red = 0,
				green = 142,
				blue = 68
			)

			field {
				name = "Server name"
				value = serverInfo.name
				inline = false
			}

			field {
				name = "Ip and Port"
				value = "${serverInfo.hostAddress}:${serverInfo.port}"
				inline = true
			}

			field {
				name = "Online count"
				value = "${serverInfo.numOfPlayers}"
				inline = true
			}

			rules["days-running"]?.let { currentDay ->
				field {
					name = "Ingame days"
					value = "$currentDay"
					inline = true
				}
			}

			field {
				name = "Online players"
				value = players.sortedBy { player -> player.name }.joinToString(separator = "\n") { player -> "**${player.name}** - ${player.score}" }
				inline = false
			}
		}
	}
}
