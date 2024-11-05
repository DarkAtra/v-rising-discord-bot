package de.darkatra.vrising.discord

import de.darkatra.vrising.discord.commands.Command
import de.darkatra.vrising.discord.migration.DatabaseMigrationService
import de.darkatra.vrising.discord.persistence.DatabaseBackupService
import de.darkatra.vrising.discord.serverstatus.ServerService
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ImportRuntimeHints
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.config.CronTask
import org.springframework.scheduling.config.IntervalTask
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import org.springframework.scheduling.support.CronTrigger
import java.time.Duration
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicBoolean

@EnableScheduling
@SpringBootApplication
@ImportRuntimeHints(BotRuntimeHints::class)
@EnableConfigurationProperties(BotProperties::class)
class Bot(
    private val botProperties: BotProperties,
    private val commands: List<Command>,
    private val databaseMigrationService: DatabaseMigrationService,
    private val serverService: ServerService,
    private val databaseBackupService: DatabaseBackupService
) : ApplicationRunner, DisposableBean, SchedulingConfigurer {

    private val logger = LoggerFactory.getLogger(javaClass)

    private var isReady = AtomicBoolean(false)
    private lateinit var kord: Kord

    override fun run(args: ApplicationArguments) = runBlocking {

        databaseMigrationService.migrateToLatestVersion()

        kord = Kord(
            token = botProperties.discordBotToken
        )

        kord.on<ChatInputCommandInteractionCreateEvent> {

            val command = commands.find { command -> command.isSupported(interaction, botProperties.adminUserIds) }
            if (command == null) {
                interaction.deferEphemeralResponse().respond {
                    content = "This command is not supported here, please refer to the documentation."
                }
                return@on
            }

            try {
                command.handle(interaction)
            } catch (e: BotException) {
                logger.error("Could not perform command '${command.getCommandName()}'.", e)
                interaction.deferEphemeralResponse().respond {
                    content = "Could not perform command. Cause: ${e.message}"
                }
            } catch (t: Throwable) {
                logger.error("An unexpected error occurred.", t)
                interaction.deferEphemeralResponse().respond {
                    content = "An unexpected error occurred."
                }
            }
        }

        kord.on<ReadyEvent> {
            val currentGlobalApplicationCommands = kord.getGlobalApplicationCommands().toList()

            // delete obsolete commands
            currentGlobalApplicationCommands
                .filterNot { applicationCommand -> commands.any { command -> command.getCommandName() == applicationCommand.name } }
                .forEach { applicationCommand ->
                    applicationCommand.delete()
                    logger.info("Successfully deleted obsolete '${applicationCommand.name}' command.")
                }

            // register commands that aren't registered yet
            commands
                .filterNot { command -> currentGlobalApplicationCommands.any { applicationCommand -> command.getCommandName() == applicationCommand.name } }
                .forEach { command ->
                    command.register(kord)
                    logger.info("Successfully registered '${command.getCommandName()}' command.")
                }

            isReady.set(true)
        }

        kord.login()
    }

    override fun destroy() {
        isReady.set(false)
        runBlocking {
            kord.shutdown()
        }
    }

    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {

        taskRegistrar.addFixedDelayTask(
            IntervalTask(
                {
                    if (isReady.get() && kord.isActive) {
                        runBlocking {
                            serverService.updateServers(kord)
                        }
                    }
                },
                botProperties.updateDelay,
                Duration.ofSeconds(5)
            )
        )

        if (botProperties.cleanupJobEnabled) {
            taskRegistrar.addCronTask(
                CronTask(
                    {
                        if (isReady.get() && kord.isActive) {
                            runBlocking {
                                serverService.cleanupInactiveServers(kord)
                            }
                        }
                    },
                    CronTrigger("0 0 0 * * *", ZoneOffset.UTC)
                )
            )
        }

        if (botProperties.databaseBackupJobEnabled) {
            taskRegistrar.addCronTask(
                CronTask(
                    {
                        if (isReady.get()) {
                            databaseBackupService.performDatabaseBackup()
                        }
                    },
                    CronTrigger("0 45 23 * * *", ZoneOffset.UTC)
                )
            )
        }
    }
}

fun main(args: Array<String>) {
    runApplication<Bot>(*args)
}
