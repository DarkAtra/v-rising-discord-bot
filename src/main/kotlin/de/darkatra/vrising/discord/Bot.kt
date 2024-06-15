package de.darkatra.vrising.discord

import de.darkatra.vrising.discord.clients.botcompanion.model.Character
import de.darkatra.vrising.discord.clients.botcompanion.model.PlayerActivity
import de.darkatra.vrising.discord.clients.botcompanion.model.PvpKill
import de.darkatra.vrising.discord.clients.botcompanion.model.VBlood
import de.darkatra.vrising.discord.commands.Command
import de.darkatra.vrising.discord.migration.DatabaseMigrationService
import de.darkatra.vrising.discord.migration.Schema
import de.darkatra.vrising.discord.persistence.model.Error
import de.darkatra.vrising.discord.persistence.model.ServerStatusMonitor
import de.darkatra.vrising.discord.serverstatus.ServerStatusMonitorService
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import org.dizitart.no2.Nitrite
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
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
@RegisterReflectionForBinding(
    BotProperties::class,
    Schema::class,
    ServerStatusMonitor::class,
    Error::class,
    Character::class,
    VBlood::class,
    PlayerActivity::class,
    PlayerActivity.Type::class,
    PvpKill::class,
    PvpKill.Player::class
)
class Bot(
    private val database: Nitrite,
    private val botProperties: BotProperties,
    private val commands: List<Command>,
    private val databaseMigrationService: DatabaseMigrationService,
    private val serverStatusMonitorService: ServerStatusMonitorService
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
                    content = """This command is not supported here, please refer to the documentation.
                        |Be sure to use the commands in the channel where you want the status message to appear.""".trimMargin()
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
            kord.getGlobalApplicationCommands()
                .filterNot { command -> commands.any { it.getCommandName() == command.name } }
                .collect { applicationCommand -> applicationCommand.delete() }
            commands.forEach { command -> command.register(kord) }
            isReady.set(true)
        }

        kord.login()
    }

    override fun destroy() {
        isReady.set(false)
        runBlocking {
            kord.shutdown()
        }
        database.close()
    }

    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {

        taskRegistrar.addFixedDelayTask(
            IntervalTask(
                {
                    if (isReady.get() && kord.isActive) {
                        runBlocking {
                            serverStatusMonitorService.updateServerStatusMonitors(kord)
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
                                serverStatusMonitorService.cleanupInactiveServerStatusMonitors(kord)
                            }
                        }
                    },
                    CronTrigger("0 0 0 * * *", ZoneOffset.UTC)
                )
            )
        }
    }
}

fun main(args: Array<String>) {
    runApplication<Bot>(*args)
}
