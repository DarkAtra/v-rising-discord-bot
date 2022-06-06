package de.darkatra.vrising.discord

import de.darkatra.vrising.discord.command.Command
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.runBlocking
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(BotProperties::class)
class Bot(
    private val botProperties: BotProperties,
    private val commands: List<Command>,
    private val serverStatusMonitorService: ServerStatusMonitorService
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) = runBlocking {

        val kord = Kord(
            token = botProperties.discordBotToken
        ) {
            enableShutdownHook = true
        }

        kord.on<ChatInputCommandInteractionCreateEvent> {

            val command = commands.find { command -> command.isSupported(interaction) }
            if (command == null) {
                interaction.deferEphemeralResponse().respond {
                    content = "This command is not supported here, please refer to the documentation."
                }
                return@on
            }

            command.handle(interaction)
        }

        kord.on<ReadyEvent> {
            commands.forEach { command -> command.register(kord) }
            serverStatusMonitorService.launchServerStatusMonitor(kord)
        }

        kord.login()
    }
}

fun main(args: Array<String>) {
    runApplication<Bot>(*args)
}
