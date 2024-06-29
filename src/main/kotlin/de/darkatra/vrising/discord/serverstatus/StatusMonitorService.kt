package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.clients.botcompanion.BotCompanionClient
import de.darkatra.vrising.discord.clients.serverquery.ServerQueryClient
import de.darkatra.vrising.discord.getDiscordChannel
import de.darkatra.vrising.discord.persistence.model.Status
import de.darkatra.vrising.discord.persistence.model.StatusMonitor
import de.darkatra.vrising.discord.serverstatus.model.ServerInfo
import de.darkatra.vrising.discord.tryCreateMessage
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.edit
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class StatusMonitorService(
    private val botProperties: BotProperties,
    private val serverQueryClient: ServerQueryClient,
    private val botCompanionClient: BotCompanionClient
) {

    private val logger by lazy { LoggerFactory.getLogger(javaClass) }

    suspend fun updateStatusMonitor(kord: Kord, statusMonitor: StatusMonitor) {

        val channel = kord.getDiscordChannel(statusMonitor.discordChannelId).getOrElse {
            logger.debug("Disabling server monitor for server '${statusMonitor.getServer().id}' because the channel '${statusMonitor.discordChannelId}' does not seem to exist.")
            statusMonitor.status = Status.INACTIVE
            return
        }

        val serverInfo = serverQueryClient.getServerStatus(
            statusMonitor.getServer().hostname,
            statusMonitor.getServer().queryPort
        ).map { serverStatus ->
            ServerInfo.of(serverStatus)
        }.getOrElse { e ->

            logger.error("Exception updating the status monitor for server '${statusMonitor.getServer().id}'.", e)
            statusMonitor.currentFailedAttempts += 1

            if (botProperties.maxRecentErrors > 0) {
                statusMonitor.addError(e, botProperties.maxRecentErrors)
            }


            if (statusMonitor.currentEmbedMessageId == null && statusMonitor.currentFailedAttempts == 1) {
                // FIXME: mention the correct command to retrieve the error messages for the status monitor
                channel.tryCreateMessage(
                    """Failed to update the status monitor for server '${statusMonitor.getServer().id}'.
                        |Please check the detailed error message using the get-server-details command.""".trimMargin()
                )
            }

            if (botProperties.maxFailedAttempts != 0 && statusMonitor.currentFailedAttempts >= botProperties.maxFailedAttempts) {
                logger.warn("Disabling server monitor for server '${statusMonitor.getServer().id}' because it exceeded the max failed attempts.")
                statusMonitor.status = Status.INACTIVE

                // FIXME: mention the correct command to re-enable the status monitor
                channel.tryCreateMessage(
                    """Disabled status monitor for server '${statusMonitor.getServer().id}' because the server did not
                        |respond successfully after ${botProperties.maxFailedAttempts} attempts.
                        |Please make sure the server is running and is accessible from the internet to use this bot.
                        |You can re-enable the server status monitor using the update-server command.""".trimMargin()
                )
            }
            return
        }

        if (statusMonitor.getServer().apiEnabled && statusMonitor.displayPlayerGearLevel) {

            val characters = botCompanionClient.getCharacters(
                statusMonitor.getServer().apiHostname!!,
                statusMonitor.getServer().apiPort!!,
                statusMonitor.getServer().getApiInterceptors()
            ).getOrElse { e ->

                logger.warn("Could not resolve characters for status monitor for server '${statusMonitor.getServer().id}'.", e)
                statusMonitor.currentFailedApiAttempts += 1

                if (botProperties.maxRecentErrors > 0) {
                    statusMonitor.addError(e, botProperties.maxRecentErrors)
                }


                if (botProperties.maxFailedApiAttempts != 0 && statusMonitor.currentFailedApiAttempts >= botProperties.maxFailedApiAttempts) {
                    logger.warn("Disabling displayPlayerGearLevel for status monitor of server '${statusMonitor.getServer().id}' because it exceeded the max failed api attempts.")
                    statusMonitor.displayPlayerGearLevel = false

                    // FIXME: mention the correct command to re-enable the status monitor
                    channel.tryCreateMessage(
                        """The status monitor for server '${statusMonitor.getServer().id}' will no longer display the players gear level because
                            |the bot companion did not respond successfully after ${botProperties.maxFailedApiAttempts} attempts.
                            |Please make sure the server-api-hostname and server-api-port are correct.
                            |You can re-enable the functionality using the update-server command.""".trimMargin()
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
        if (currentEmbedMessageId != null) {
            try {
                channel.getMessage(Snowflake(currentEmbedMessageId))
                    .edit { embed(embedCustomizer) }

                statusMonitor.currentFailedAttempts = 0

                logger.debug("Successfully updated the status monitor for server '${statusMonitor.getServer().id}'.")
                return
            } catch (e: Exception) {
                statusMonitor.currentEmbedMessageId = null
            }
        }

        statusMonitor.currentEmbedMessageId = channel.createEmbed(embedCustomizer).id.toString()
        statusMonitor.currentFailedAttempts = 0

        logger.debug("Successfully updated the status and persisted the embedId for server monitor of server '${statusMonitor.getServer().id}'.")
    }
}
