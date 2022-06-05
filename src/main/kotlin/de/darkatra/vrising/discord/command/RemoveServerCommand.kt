package de.darkatra.vrising.discord.command

import de.darkatra.vrising.discord.ServerStatusMonitorService
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.string

class RemoveServerCommand(
	private val name: String = "remove-server",
	private val description: String = "Removes a server from the status monitor."
) : Command {

	override fun getCommandName(): String = name

	override suspend fun register(kord: Kord) {

		kord.createGlobalChatInputCommand(
			name = name,
			description = description
		) {

			string(
				name = "server-status-monitor-id",
				description = "The id of the server status monitor."
			) {
				required = true
			}
		}
	}

	override suspend fun handle(interaction: ChatInputCommandInteraction) {

		val command = interaction.command
		val serverStatusMonitorId = command.strings["server-status-monitor-id"]!!

		ServerStatusMonitorService.removeServerStatusMonitor(serverStatusMonitorId)

		val response = interaction.deferEphemeralResponse()
		response.respond {
			content = "Removed monitor with id '$serverStatusMonitorId'."
		}
	}
}
