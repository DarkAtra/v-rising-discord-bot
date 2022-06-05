package de.darkatra.vrising.discord.command

import com.fasterxml.uuid.Generators
import de.darkatra.vrising.discord.ServerStatusMonitor
import de.darkatra.vrising.discord.ServerStatusMonitorService
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.int
import dev.kord.rest.builder.interaction.string

class AddServerCommand(
	private val name: String = "add-server",
	private val description: String = "Adds a server to the status monitor."
) : Command {

	override fun getCommandName(): String = name

	override suspend fun register(kord: Kord) {

		kord.createGlobalChatInputCommand(
			name = name,
			description = description
		) {

			string(
				name = "server-hostname",
				description = "The hostname of the server to add a status monitor for."
			) {
				required = true
			}

			int(
				name = "server-query-port",
				description = "The query port of the server to add a status monitor for."
			) {
				required = true
			}
		}
	}

	override suspend fun handle(interaction: ChatInputCommandInteraction) {

		val command = interaction.command
		val hostName = command.strings["server-hostname"]!!
		val queryPort = Math.toIntExact(command.integers["server-query-port"]!!)
		val channelId = interaction.channelId

		ServerStatusMonitorService.putServerStatusMonitor(ServerStatusMonitor(
			id = Generators.timeBasedGenerator().generate().toString(),
			hostName = hostName,
			queryPort = queryPort,
			discordChannelId = channelId
		))

		val response = interaction.deferEphemeralResponse()
		response.respond {
			content = "Added monitor for '${hostName}:${queryPort}' to channel '$channelId'."
		}
	}
}
