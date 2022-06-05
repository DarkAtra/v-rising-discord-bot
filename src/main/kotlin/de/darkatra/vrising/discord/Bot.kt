package de.darkatra.vrising.discord

import de.darkatra.vrising.discord.command.AddServerCommand
import de.darkatra.vrising.discord.command.Command
import de.darkatra.vrising.discord.command.ListServersCommand
import de.darkatra.vrising.discord.command.RemoveServerCommand
import de.darkatra.vrising.serverquery.ServerQueryClient
import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on

suspend fun main(args: Array<String>) {

	if (args.size != 1) {
		println("Expected exactly one argument containing the discord bot token.")
		return
	}

	val kord = Kord(
		token = args[0]
	) {
		enableShutdownHook = true
	}

	val commands: List<Command> = listOf(
		AddServerCommand(),
		ListServersCommand(),
		RemoveServerCommand()
	)

	kord.on<ChatInputCommandInteractionCreateEvent> {
		val command = commands.find { command -> command.isSupported(interaction) } ?: return@on
		command.handle(interaction)
	}

	kord.on<ReadyEvent> {
		commands.forEach { command -> command.register(kord) }
		ServerStatusMonitorService.launchServerStatusMonitor(kord)
	}

	kord.login()

	ServerStatusMonitorService.destroy()
	ServerQueryClient.destroy()
}

