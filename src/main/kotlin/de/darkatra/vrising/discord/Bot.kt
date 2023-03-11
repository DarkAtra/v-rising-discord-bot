package de.darkatra.vrising.discord

import de.darkatra.vrising.discord.command.Command
import de.darkatra.vrising.discord.command.ValidationException
import de.darkatra.vrising.discord.migration.DatabaseMigrationService
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.dizitart.no2.Nitrite
import org.springframework.beans.factory.DisposableBean
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(BotProperties::class)
class Bot(
    private val database: Nitrite,
    private val botProperties: BotProperties,
    private val commands: List<Command>,
    private val databaseMigrationService: DatabaseMigrationService,
    private val serverStatusMonitorService: ServerStatusMonitorService
) : ApplicationRunner, DisposableBean {

    override fun run(args: ApplicationArguments) = runBlocking {

        databaseMigrationService.migrateToLatestVersion()

        val kord = Kord(
            token = botProperties.discordBotToken
        ) {
            enableShutdownHook = true
        }

        kord.on<ChatInputCommandInteractionCreateEvent> {

            val command = commands.find { command -> command.isSupported(interaction) }
            if (command == null) {
                interaction.deferEphemeralResponse().respond {
                    content = """This command is not supported here, please refer to the documentation.
                        |Be sure to use the commands in the channel where you want the status message to appear.""".trimMargin()
                }
                return@on
            }

            try {
                command.handle(interaction)
            } catch (e: ValidationException) {
                interaction.deferEphemeralResponse().respond {
                    content = "Could not perform command. Cause: ${e.message}"
                }
            }
        }

        kord.on<ReadyEvent> {
            kord.getGlobalApplicationCommands().onEach { applicationCommand -> applicationCommand.delete() }
            commands.forEach { command -> command.register(kord) }

            serverStatusMonitorService.launchServerStatusMonitor(kord)
        }

        kord.login()
    }

    override fun destroy() {
        database.close()
    }
}

fun main(args: Array<String>) {
    runApplication<Bot>(*args)
}
