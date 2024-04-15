package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.clients.botcompanion.model.PlayerActivity
import de.darkatra.vrising.discord.clients.botcompanion.model.PvpKill
import de.darkatra.vrising.discord.serverstatus.exceptions.InvalidDiscordChannelException
import de.darkatra.vrising.discord.serverstatus.exceptions.OutdatedServerStatusMonitorException
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
            updatePlayerActivityFeed(kord, serverStatusMonitor)
            updatePvpKillFeed(kord, serverStatusMonitor)
            try {
                serverStatusMonitorRepository.updateServerStatusMonitor(serverStatusMonitor)
            } catch (e: OutdatedServerStatusMonitorException) {
                logger.debug("Server status monitor was updated or deleted by another thread. Will ignore this exception and proceed as usual.", e)
            }
        }
    }

    suspend fun updateServerStatusMonitor(kord: Kord, serverStatusMonitor: ServerStatusMonitor) {

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

                    serverStatusMonitor.currentFailedAttempts = 0

                    logger.debug("Successfully updated the status of server monitor: ${serverStatusMonitor.id}")
                    return
                } catch (e: EntityNotFoundException) {
                    serverStatusMonitor.currentEmbedMessageId = null
                }
            }

            serverStatusMonitor.currentEmbedMessageId = channel.createEmbed(embedCustomizer).id.toString()
            serverStatusMonitor.currentFailedAttempts = 0

            logger.debug("Successfully updated the status and persisted the embedId of server monitor: ${serverStatusMonitor.id}")

        } catch (e: InvalidDiscordChannelException) {
            logger.debug("Disabling server monitor '${serverStatusMonitor.id}' because the channel '${e.discordChannelId}' does not seem to exist")
            serverStatusMonitor.status = ServerStatusMonitorStatus.INACTIVE
        } catch (e: Exception) {

            logger.error("Exception fetching the status of ${serverStatusMonitor.id}", e)
            serverStatusMonitor.currentFailedAttempts += 1

            if (botProperties.maxRecentErrors > 0) {
                serverStatusMonitor.recentErrors = serverStatusMonitor.recentErrors
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

            if (serverStatusMonitor.currentEmbedMessageId == null && serverStatusMonitor.currentFailedAttempts == 1) {
                val channel = getDiscordChannel(kord, serverStatusMonitor.discordChannelId)
                channel.createMessage(
                    """The status check for your status monitor '${serverStatusMonitor.id}' failed.
                        |Please check the detailed error message using the get-server-details command.""".trimMargin()
                )
            }

            if (botProperties.maxFailedAttempts != 0 && serverStatusMonitor.currentFailedAttempts >= botProperties.maxFailedAttempts) {
                logger.warn("Disabling server monitor '${serverStatusMonitor.id}' because it exceeded the max failed attempts.")
                serverStatusMonitor.status = ServerStatusMonitorStatus.INACTIVE

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

    suspend fun updatePlayerActivityFeed(kord: Kord, serverStatusMonitor: ServerStatusMonitor) {

        try {
            val playerActivityDiscordChannelId = serverStatusMonitor.playerActivityDiscordChannelId ?: return
            val playerActivityChannel = getDiscordChannel(kord, playerActivityDiscordChannelId)
            val playerActivities = serverInfoResolver.getPlayerActivities(serverStatusMonitor)

            playerActivities
                .filter { playerActivity -> playerActivity.occurred.isAfter(serverStatusMonitor.lastUpdated) }
                .sortedWith(Comparator.comparing(PlayerActivity::occurred))
                .forEach { playerActivity ->
                    val action = when (playerActivity.type) {
                        PlayerActivity.Type.CONNECTED -> "joined"
                        PlayerActivity.Type.DISCONNECTED -> "left"
                    }
                    playerActivityChannel.createMessage(
                        "<t:${playerActivity.occurred.epochSecond}>: ${playerActivity.playerName} $action the server."
                    )
                }

            logger.debug("Successfully updated the player activity feed of server monitor: ${serverStatusMonitor.id}")

        } catch (e: InvalidDiscordChannelException) {
            logger.debug("Disabling player activity feed for server monitor '${serverStatusMonitor.id}' because the channel '${e.discordChannelId}' does not seem to exist")
            serverStatusMonitor.playerActivityDiscordChannelId = null
        } catch (e: Exception) {
            logger.error("Exception updating the player activity feed of ${serverStatusMonitor.id}", e)
        }
    }

    suspend fun updatePvpKillFeed(kord: Kord, serverStatusMonitor: ServerStatusMonitor) {

        try {
            val pvpKillFeedDiscordChannelId = serverStatusMonitor.pvpKillFeedDiscordChannelId ?: return
            val pvpKillFeedChannel = getDiscordChannel(kord, pvpKillFeedDiscordChannelId)
            val pvpKills = serverInfoResolver.getPvpKills(serverStatusMonitor)

            pvpKills
                .filter { pvpKill -> pvpKill.occurred.isAfter(serverStatusMonitor.lastUpdated) }
                .sortedWith(Comparator.comparing(PvpKill::occurred))
                .forEach { pvpKill ->
                    pvpKillFeedChannel.createMessage(
                        "<t:${pvpKill.occurred.epochSecond}>: ${pvpKill.killer.name} (${pvpKill.killer.gearLevel}) killed ${pvpKill.victim.name} (${pvpKill.victim.gearLevel})."
                    )
                }

            logger.debug("Successfully updated the pvp kill feed of server monitor: ${serverStatusMonitor.id}")

        } catch (e: InvalidDiscordChannelException) {
            logger.debug("Disabling pvp kill feed for server monitor '${serverStatusMonitor.id}' because the channel '${e.discordChannelId}' does not seem to exist")
            serverStatusMonitor.pvpKillFeedDiscordChannelId = null
        } catch (e: Exception) {
            logger.error("Exception updating the pvp kill feed of ${serverStatusMonitor.id}", e)
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
