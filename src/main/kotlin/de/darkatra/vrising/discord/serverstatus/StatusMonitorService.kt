package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.InvalidDiscordChannelException
import de.darkatra.vrising.discord.clients.botcompanion.BotCompanionClient
import de.darkatra.vrising.discord.clients.serverquery.CancellationException
import de.darkatra.vrising.discord.clients.serverquery.ServerQueryClient
import de.darkatra.vrising.discord.commands.ConfigureStatusMonitorCommand
import de.darkatra.vrising.discord.commands.GetStatusMonitorDetailsCommand
import de.darkatra.vrising.discord.getDiscordChannel
import de.darkatra.vrising.discord.persistence.model.Status
import de.darkatra.vrising.discord.persistence.model.StatusMonitor
import de.darkatra.vrising.discord.serverstatus.model.ServerInfo
import de.darkatra.vrising.discord.tryCreateMessage
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.edit
import dev.kord.core.exception.EntityNotFoundException
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class StatusMonitorService(
    private val botProperties: BotProperties,
    private val serverQueryClient: ServerQueryClient,
    private val botCompanionClient: BotCompanionClient,
    private val getStatusMonitorDetailsCommand: GetStatusMonitorDetailsCommand,
    private val configureStatusMonitorCommand: ConfigureStatusMonitorCommand
) {

    private val logger by lazy { LoggerFactory.getLogger(javaClass) }

    suspend fun updateStatusMonitor(kord: Kord, statusMonitor: StatusMonitor) {

        logger.debug("Attempting to update the server monitor for server '${statusMonitor.getServer().id}'...")

        val channel = kord.getDiscordChannel(statusMonitor.discordChannelId).getOrElse { e ->
            when (e) {
                is InvalidDiscordChannelException -> {
                    logger.debug("Disabling server monitor for server '${statusMonitor.getServer().id}' because the channel '${statusMonitor.discordChannelId}' does not seem to exist.")
                    statusMonitor.status = Status.INACTIVE
                }

                else -> {
                    statusMonitor.currentFailedAttempts += 1
                    statusMonitor.addError(e, botProperties.maxRecentErrors)
                    disableStatusMonitorIfNecessary(statusMonitor)
                }
            }
            return
        }

        val serverInfo = serverQueryClient.getServerStatus(
            statusMonitor.getServer().hostname,
            statusMonitor.getServer().queryPort
        ).map { serverStatus ->
            ServerInfo.of(serverStatus)
        }.getOrElse { e ->

            if (e is CancellationException) {
                logger.debug("Server query was canceled.", e)
                return
            }

            logger.error("Exception updating the status monitor for server '${statusMonitor.getServer().id}'.", e)
            statusMonitor.currentFailedAttempts += 1
            statusMonitor.addError(e, botProperties.maxRecentErrors)

            if (statusMonitor.currentEmbedMessageId == null && statusMonitor.currentFailedAttempts == 1) {
                channel.tryCreateMessage(
                    """Failed to update the status monitor for server '${statusMonitor.getServer().id}'.
                        |Please check the detailed error message using the ${getStatusMonitorDetailsCommand.getCommandName()} command.""".trimMargin()
                )
            }

            disableStatusMonitorIfNecessary(statusMonitor) {
                channel.tryCreateMessage(
                    """Disabled status monitor for server '${statusMonitor.getServer().id}' because the server did not
                        |respond successfully after ${botProperties.maxFailedAttempts} attempts.
                        |Please make sure the server is running and is accessible from the internet to use this bot.
                        |You can re-enable this functionality using the ${configureStatusMonitorCommand.getCommandName()} command.""".trimMargin()
                )
            }
            return
        }

        if (statusMonitor.getServer().apiEnabled && statusMonitor.displayPlayerGearLevel) {

            val characters = botCompanionClient.getCharacters(
                statusMonitor.getServer().apiHostname!!,
                statusMonitor.getServer().apiPort!!,
                statusMonitor.getServer().apiUsername,
                statusMonitor.getServer().apiPassword,
                statusMonitor.getServer().useSecureTransport
            ).getOrElse { e ->

                logger.warn("Could not resolve characters for status monitor for server '${statusMonitor.getServer().id}'.", e)
                statusMonitor.currentFailedApiAttempts += 1
                statusMonitor.addError(e, botProperties.maxRecentErrors)

                disableBotCompanionFeaturesIfNecessary(statusMonitor) {
                    channel.tryCreateMessage(
                        """The status monitor for server '${statusMonitor.getServer().id}' will no longer display the players gear level because
                            |the bot companion did not respond successfully after ${botProperties.maxFailedApiAttempts} attempts.
                            |Please make sure the server-api-hostname and server-api-port are correct.
                            |You can re-enable this functionality using the ${configureStatusMonitorCommand.getCommandName()} command.""".trimMargin()
                    )
                }
                return
            }

            serverInfo.enrichCompanionData(characters)
            statusMonitor.currentFailedApiAttempts = 0
        }

        val embedCustomizer: (embedBuilder: EmbedBuilder) -> Unit = { embedBuilder ->
            ServerStatusEmbed.buildEmbed(
                serverInfo,
                statusMonitor.getServer().apiEnabled,
                statusMonitor.displayServerDescription,
                statusMonitor.displayPlayerGearLevel,
                embedBuilder
            )
        }

        val currentEmbedMessageId = statusMonitor.currentEmbedMessageId
        when {
            currentEmbedMessageId != null -> try {

                channel.getMessage(Snowflake(currentEmbedMessageId))
                    .edit { embed(embedCustomizer) }

                statusMonitor.currentFailedAttempts = 0

                logger.debug("Successfully updated the status monitor for server '${statusMonitor.getServer().id}'.")
            } catch (e: EntityNotFoundException) {
                statusMonitor.currentEmbedMessageId = null
            } catch (e: Exception) {
                logger.warn("Could not update status embed for server '${statusMonitor.getServer().id}'", e)

                statusMonitor.currentFailedAttempts += 1
                statusMonitor.addError(e, botProperties.maxRecentErrors)
            }

            else -> try {

                statusMonitor.currentEmbedMessageId = channel.createEmbed(embedCustomizer).id.toString()
                statusMonitor.currentFailedAttempts = 0

                logger.debug("Successfully updated the status and persisted the embedId for server monitor of server '${statusMonitor.getServer().id}'.")
            } catch (e: Exception) {
                logger.warn("Could not create status embed for server '${statusMonitor.getServer().id}'", e)

                statusMonitor.currentFailedAttempts += 1
                statusMonitor.addError(e, botProperties.maxRecentErrors)
            }
        }
    }

    private suspend fun disableStatusMonitorIfNecessary(statusMonitor: StatusMonitor, block: suspend () -> Unit = {}) {

        if (botProperties.maxFailedAttempts != 0 && statusMonitor.currentFailedAttempts >= botProperties.maxFailedAttempts) {
            logger.warn("Disabling server monitor for server '${statusMonitor.getServer().id}' because it exceeded the max failed attempts.")
            statusMonitor.status = Status.INACTIVE
            block()
        }
    }

    private suspend fun disableBotCompanionFeaturesIfNecessary(statusMonitor: StatusMonitor, block: suspend () -> Unit = {}) {

        if (botProperties.maxFailedApiAttempts != 0 && statusMonitor.currentFailedApiAttempts >= botProperties.maxFailedApiAttempts) {
            logger.warn("Disabling displayPlayerGearLevel for status monitor of server '${statusMonitor.getServer().id}' because it exceeded the max failed api attempts.")
            statusMonitor.displayPlayerGearLevel = false
            block()
        }
    }
}
