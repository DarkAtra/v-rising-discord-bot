package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.serverstatus.model.Error
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitorStatus
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.edit
import dev.kord.core.exception.EntityNotFoundException
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.modify.embed
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ServerStatusMonitorService(
    private val serverStatusMonitorRepository: ServerStatusMonitorRepository,
    private val serverInfoResolver: ServerInfoResolver,
    private val botProperties: BotProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun updateServerStatusMonitors(kord: Kord) {
        serverStatusMonitorRepository.getServerStatusMonitors(status = ServerStatusMonitorStatus.ACTIVE).forEach { serverStatusMonitor ->
            updateServerStatusMonitor(kord, serverStatusMonitor)
        }
    }

    suspend fun updateServerStatusMonitor(kord: Kord, serverStatusMonitor: ServerStatusMonitor) {

        val serverStatusMonitorBuilder = serverStatusMonitor.builder()

        try {

            val channel = getDiscordChannel(kord, serverStatusMonitor.discordChannelId)
            val serverInfo = serverInfoResolver.getServerInfo(serverStatusMonitor)

            val embedCustomizer: (embedBuilder: EmbedBuilder) -> Unit = { embedBuilder ->
                ServerStatusEmbed.buildEmbed(
                    serverInfo,
                    serverStatusMonitor.apiEnabled,
                    serverStatusMonitor.displayServerDescription,
                    serverStatusMonitor.displayPlayerGearLevel,
                    embedBuilder
                )
            }

            val currentEmbedMessageId = serverStatusMonitor.currentEmbedMessageId
            if (currentEmbedMessageId != null) {
                try {
                    channel.getMessage(Snowflake(currentEmbedMessageId))
                        .edit { embed(embedCustomizer) }

                    serverStatusMonitorBuilder.currentFailedAttempts = 0
                    serverStatusMonitorRepository.putServerStatusMonitor(serverStatusMonitorBuilder.build())

                    logger.debug("Successfully updated the status of server monitor: ${serverStatusMonitor.id}")
                    return
                } catch (e: EntityNotFoundException) {
                    serverStatusMonitorBuilder.currentEmbedMessageId = null
                }
            }

            serverStatusMonitorBuilder.currentEmbedMessageId = channel.createEmbed(embedCustomizer).id.toString()
            serverStatusMonitorBuilder.currentFailedAttempts = 0

            serverStatusMonitorRepository.putServerStatusMonitor(serverStatusMonitorBuilder.build())

            logger.debug("Successfully updated the status and persisted the embedId of server monitor: ${serverStatusMonitor.id}")

        } catch (e: InvalidDiscordChannelException) {
            logger.debug("Disabling server monitor '${serverStatusMonitor.id}' because the channel '${e.discordChannelId}' does not seem to exist")
            serverStatusMonitorRepository.disableServerStatusMonitor(serverStatusMonitor)
        } catch (e: Exception) {

            logger.error("Exception while fetching the status of ${serverStatusMonitor.id}", e)
            serverStatusMonitorBuilder.currentFailedAttempts += 1

            if (botProperties.maxRecentErrors > 0) {
                serverStatusMonitorBuilder.recentErrors = serverStatusMonitorBuilder.recentErrors
                    .takeLast((botProperties.maxRecentErrors - 1).coerceAtLeast(0))
                    .toMutableList()
                    .apply {
                        add(
                            Error(
                                message = "${e::class.simpleName}: ${e.message}",
                                timestamp = Instant.now().toString()
                            )
                        )
                    }
            }

            serverStatusMonitorRepository.putServerStatusMonitor(serverStatusMonitorBuilder.build())

            if (botProperties.maxFailedAttempts != 0 && serverStatusMonitor.currentFailedAttempts >= botProperties.maxFailedAttempts) {
                logger.debug("Disabling server monitor '${serverStatusMonitor.id}' because it exceeded the max failed attempts.")
                serverStatusMonitorRepository.disableServerStatusMonitor(serverStatusMonitor)

                val channel = getDiscordChannel(kord, serverStatusMonitor.discordChannelId)
                channel.createMessage(
                    """Disabled server status monitor '${serverStatusMonitor.id}' because the server did not
                        |respond after ${botProperties.maxFailedAttempts} attempts.
                        |Please make sure the server is running and is accessible from the internet to use this bot.
                        |You can re-enable the server status monitor with the update-server command.""".trimMargin()
                )
            }
        }
    }

    private suspend fun getDiscordChannel(kord: Kord, discordChannelId: String): MessageChannelBehavior {
        val channel = kord.getChannel(Snowflake(discordChannelId))
        if (channel == null || channel !is MessageChannelBehavior) {
            throw InvalidDiscordChannelException("Discord Channel '$discordChannelId' does not exist.", discordChannelId)
        }
        return channel
    }
}
