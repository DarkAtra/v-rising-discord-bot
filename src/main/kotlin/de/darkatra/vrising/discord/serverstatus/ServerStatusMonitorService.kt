package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.clients.botcompanion.BotCompanionClient
import de.darkatra.vrising.discord.clients.botcompanion.model.PlayerActivity
import de.darkatra.vrising.discord.clients.botcompanion.model.PvpKill
import de.darkatra.vrising.discord.clients.serverquery.ServerQueryClient
import de.darkatra.vrising.discord.getDiscordChannel
import de.darkatra.vrising.discord.persistence.ServerStatusMonitorRepository
import de.darkatra.vrising.discord.persistence.model.ServerStatusMonitor
import de.darkatra.vrising.discord.persistence.model.ServerStatusMonitorStatus
import de.darkatra.vrising.discord.serverstatus.exceptions.OutdatedServerStatusMonitorException
import de.darkatra.vrising.discord.serverstatus.model.ServerInfo
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.edit
import dev.kord.core.exception.EntityNotFoundException
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.embed
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.support.BasicAuthenticationInterceptor
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class ServerStatusMonitorService(
    private val serverStatusMonitorRepository: ServerStatusMonitorRepository,
    private val serverQueryClient: ServerQueryClient,
    private val botCompanionClient: BotCompanionClient,
    private val botProperties: BotProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun updateServerStatusMonitors(kord: Kord) {

        serverStatusMonitorRepository.getServerStatusMonitors(status = ServerStatusMonitorStatus.ACTIVE).forEach { serverStatusMonitor ->

            MDC.put("server-status-monitor-id", serverStatusMonitor.id)

            try {
                updateServerStatusMonitor(kord, serverStatusMonitor)
                updatePlayerActivityFeed(kord, serverStatusMonitor)
                updatePvpKillFeed(kord, serverStatusMonitor)
            } catch (e: Exception) {
                logger.error("Unhandled error updating status monitor '${serverStatusMonitor.id}'. Please report this issue: https://github.com/DarkAtra/v-rising-discord-bot/issues/new/choose")
            }
            try {
                serverStatusMonitorRepository.updateServerStatusMonitor(serverStatusMonitor)
            } catch (e: OutdatedServerStatusMonitorException) {
                logger.debug("Server status monitor was updated or deleted by another thread. Will ignore this exception and proceed as usual.", e)
            }

            MDC.clear()
        }
    }

    suspend fun cleanupInactiveServerStatusMonitors(kord: Kord) {

        val sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS)
        val inactiveServerStatusMonitors = serverStatusMonitorRepository.getServerStatusMonitors(status = ServerStatusMonitorStatus.INACTIVE)
        inactiveServerStatusMonitors.forEach { serverStatusMonitor ->
            if (serverStatusMonitor.lastUpdated.isBefore(sevenDaysAgo)) {
                serverStatusMonitorRepository.removeServerStatusMonitor(serverStatusMonitor.id)
            }
        }

        logger.info("Successfully removed ${inactiveServerStatusMonitors.count()} inactive server status monitors.")
    }

    private suspend fun updateServerStatusMonitor(kord: Kord, serverStatusMonitor: ServerStatusMonitor) {

        if (!serverStatusMonitor.embedEnabled) {
            logger.debug("Skipping server monitor '${serverStatusMonitor.id}' because embedEnabled is false.")
            return
        }

        val channel = kord.getDiscordChannel(serverStatusMonitor.discordChannelId).getOrElse {
            logger.debug("Disabling server monitor '${serverStatusMonitor.id}' because the channel '${serverStatusMonitor.discordChannelId}' does not seem to exist.")
            serverStatusMonitor.status = ServerStatusMonitorStatus.INACTIVE
            return
        }

        val serverInfo = serverQueryClient.getServerStatus(
            serverStatusMonitor.hostname,
            serverStatusMonitor.queryPort
        ).map { serverStatus ->
            ServerInfo.of(serverStatus)
        }.getOrElse { e ->

            logger.error("Exception fetching the status of '${serverStatusMonitor.id}'.", e)
            serverStatusMonitor.currentFailedAttempts += 1

            if (botProperties.maxRecentErrors > 0) {
                serverStatusMonitor.addError(e, botProperties.maxRecentErrors)
            }

            try {

                if (serverStatusMonitor.currentEmbedMessageId == null && serverStatusMonitor.currentFailedAttempts == 1) {
                    channel.createMessage(
                        """The status check for your status monitor '${serverStatusMonitor.id}' failed.
                            |Please check the detailed error message using the get-server-details command.""".trimMargin()
                    )
                }

                if (botProperties.maxFailedAttempts != 0 && serverStatusMonitor.currentFailedAttempts >= botProperties.maxFailedAttempts) {
                    logger.warn("Disabling server monitor '${serverStatusMonitor.id}' because it exceeded the max failed attempts.")
                    serverStatusMonitor.status = ServerStatusMonitorStatus.INACTIVE

                    channel.createMessage(
                        """Disabled server status monitor '${serverStatusMonitor.id}' because the server did not
                            |respond successfully after ${botProperties.maxFailedAttempts} attempts.
                            |Please make sure the server is running and is accessible from the internet to use this bot.
                            |You can re-enable the server status monitor using the update-server command.""".trimMargin()
                    )
                }
            } catch (e: Exception) {
                logger.warn("Could not post status message for monitor '${serverStatusMonitor.id}'.", e)
            }
            return
        }

        if (serverStatusMonitor.apiEnabled && serverStatusMonitor.displayPlayerGearLevel) {

            val characters = botCompanionClient.getCharacters(
                serverStatusMonitor.apiHostname!!,
                serverStatusMonitor.apiPort!!,
                getInterceptors(serverStatusMonitor)
            ).getOrElse { e ->

                logger.warn("Could not resolve characters for server monitor '${serverStatusMonitor.id}'. Player Gear level will not be displayed.", e)
                serverStatusMonitor.currentFailedApiAttempts += 1

                if (botProperties.maxRecentErrors > 0) {
                    serverStatusMonitor.addError(e, botProperties.maxRecentErrors)
                }

                try {

                    if (botProperties.maxFailedApiAttempts != 0 && serverStatusMonitor.currentFailedApiAttempts >= botProperties.maxFailedApiAttempts) {
                        logger.warn("Disabling displayPlayerGearLevel for server monitor '${serverStatusMonitor.id}' because it exceeded the max failed api attempts.")
                        serverStatusMonitor.displayPlayerGearLevel = false

                        channel.createMessage(
                            """Disabled displayPlayerGearLevel for server status monitor '${serverStatusMonitor.id}' because
                                |the bot companion did not respond successfully after ${botProperties.maxFailedApiAttempts} attempts.
                                |Please make sure the server-api-hostname and server-api-port are correct.
                                |You can re-enable the functionality using the update-server command.""".trimMargin()
                        )
                    }
                } catch (e: Exception) {
                    logger.warn("Could not post status message for monitor '${serverStatusMonitor.id}'", e)
                }
                return
            }

            serverInfo.enrichCompanionData(characters)
            serverStatusMonitor.currentFailedApiAttempts = 0
        }

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

                logger.debug("Successfully updated the status of server monitor '${serverStatusMonitor.id}'.")
                return
            } catch (e: EntityNotFoundException) {
                serverStatusMonitor.currentEmbedMessageId = null
            } catch (e: Exception) {
                logger.warn("Could not update status embed for monitor '${serverStatusMonitor.id}'", e)
            }
        }

        try {
            serverStatusMonitor.currentEmbedMessageId = channel.createEmbed(embedCustomizer).id.toString()
            serverStatusMonitor.currentFailedAttempts = 0

            logger.debug("Successfully updated the status and persisted the embedId of server monitor '${serverStatusMonitor.id}'.")
        } catch (e: Exception) {
            logger.warn("Could not create status embed for monitor '${serverStatusMonitor.id}'", e)
        }
    }

    private suspend fun updatePlayerActivityFeed(kord: Kord, serverStatusMonitor: ServerStatusMonitor) {

        if (!serverStatusMonitor.apiEnabled) {
            logger.debug("Skipping player activity feed update for server monitor '${serverStatusMonitor.id}' because apiEnabled is false.")
            return
        }

        val playerActivityDiscordChannelId = serverStatusMonitor.playerActivityDiscordChannelId ?: return
        val playerActivityChannel = kord.getDiscordChannel(playerActivityDiscordChannelId).getOrElse {
            logger.debug("Disabling player activity feed for server monitor '${serverStatusMonitor.id}' because the channel '${playerActivityDiscordChannelId}' does not seem to exist.")
            serverStatusMonitor.playerActivityDiscordChannelId = null
            return
        }

        val playerActivities = botCompanionClient.getPlayerActivities(
            serverStatusMonitor.apiHostname!!,
            serverStatusMonitor.apiPort!!,
            getInterceptors(serverStatusMonitor)
        ).getOrElse { e ->

            logger.error("Exception updating the player activity feed of '${serverStatusMonitor.id}'", e)
            serverStatusMonitor.currentFailedApiAttempts += 1

            if (botProperties.maxRecentErrors > 0) {
                serverStatusMonitor.addError(e, botProperties.maxRecentErrors)
            }

            try {
                if (botProperties.maxFailedApiAttempts != 0 && serverStatusMonitor.currentFailedApiAttempts >= botProperties.maxFailedApiAttempts) {
                    logger.warn("Disabling the player activity feed for server monitor '${serverStatusMonitor.id}' because it exceeded the max failed api attempts.")
                    serverStatusMonitor.playerActivityDiscordChannelId = null

                    playerActivityChannel.createMessage(
                        """Disabled the player activity feed for server status monitor '${serverStatusMonitor.id}' because
                            |the bot companion did not respond successfully after ${botProperties.maxFailedApiAttempts} attempts.
                            |Please make sure the server-api-hostname and server-api-port are correct.
                            |You can re-enable the functionality using the update-server command.""".trimMargin()
                    )
                }
            } catch (e: Exception) {
                logger.warn("Could not post status message for monitor '${serverStatusMonitor.id}'", e)
            }
            return
        }

        serverStatusMonitor.currentFailedApiAttempts = 0

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

        logger.debug("Successfully updated the player activity feed of server monitor '${serverStatusMonitor.id}'.")
    }

    private suspend fun updatePvpKillFeed(kord: Kord, serverStatusMonitor: ServerStatusMonitor) {

        if (!serverStatusMonitor.apiEnabled) {
            logger.debug("Skipping pvp kill feed update for server monitor '${serverStatusMonitor.id}' because apiEnabled is false.")
            return
        }

        val pvpKillFeedDiscordChannelId = serverStatusMonitor.pvpKillFeedDiscordChannelId ?: return
        val pvpKillFeedChannel = kord.getDiscordChannel(pvpKillFeedDiscordChannelId).getOrElse {
            logger.debug("Disabling pvp kill feed for server monitor '${serverStatusMonitor.id}' because the channel '${pvpKillFeedDiscordChannelId}' does not seem to exist.")
            serverStatusMonitor.pvpKillFeedDiscordChannelId = null
            return
        }

        val pvpKills = botCompanionClient.getPvpKills(
            serverStatusMonitor.apiHostname!!,
            serverStatusMonitor.apiPort!!,
            getInterceptors(serverStatusMonitor)
        ).getOrElse { e ->

            logger.error("Exception updating the pvp kill feed of ${serverStatusMonitor.id}", e)
            serverStatusMonitor.currentFailedApiAttempts += 1

            if (botProperties.maxRecentErrors > 0) {
                serverStatusMonitor.addError(e, botProperties.maxRecentErrors)
            }

            try {
                if (botProperties.maxFailedApiAttempts != 0 && serverStatusMonitor.currentFailedApiAttempts >= botProperties.maxFailedApiAttempts) {
                    logger.warn("Disabling the pvp kill feed for server monitor '${serverStatusMonitor.id}' because it exceeded the max failed api attempts.")
                    serverStatusMonitor.pvpKillFeedDiscordChannelId = null

                    pvpKillFeedChannel.createMessage(
                        """Disabled the pvp kill feed for server status monitor '${serverStatusMonitor.id}' because
                            |the bot companion did not respond successfully after ${botProperties.maxFailedApiAttempts} attempts.
                            |Please make sure the server-api-hostname and server-api-port are correct.
                            |You can re-enable the functionality using the update-server command.""".trimMargin()
                    )
                }
            } catch (e: Exception) {
                logger.warn("Could not post status message for monitor '${serverStatusMonitor.id}'", e)
            }
            return
        }

        serverStatusMonitor.currentFailedApiAttempts = 0

        pvpKills
            .filter { pvpKill -> pvpKill.occurred.isAfter(serverStatusMonitor.lastUpdated) }
            .sortedWith(Comparator.comparing(PvpKill::occurred))
            .forEach { pvpKill ->
                pvpKillFeedChannel.createMessage(
                    "<t:${pvpKill.occurred.epochSecond}>: ${pvpKill.killer.name} (${pvpKill.killer.gearLevel}) killed ${pvpKill.victim.name} (${pvpKill.victim.gearLevel})."
                )
            }

        logger.debug("Successfully updated the pvp kill feed of server monitor '${serverStatusMonitor.id}'.")
    }

    private fun getInterceptors(serverStatusMonitor: ServerStatusMonitor): List<ClientHttpRequestInterceptor> {

        val (_, _, _, _, _, _, _, _, _, _, apiUsername, apiPassword, _, _, _, _, _, _) = serverStatusMonitor

        return when (apiUsername != null && apiPassword != null) {
            true -> listOf(BasicAuthenticationInterceptor(apiUsername, apiPassword))
            false -> emptyList()
        }
    }
}
