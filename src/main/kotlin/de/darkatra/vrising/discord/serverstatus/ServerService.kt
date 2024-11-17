package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.persistence.OutdatedServerException
import de.darkatra.vrising.discord.persistence.ServerRepository
import de.darkatra.vrising.discord.persistence.model.Server
import de.darkatra.vrising.discord.persistence.model.Status
import de.darkatra.vrising.discord.persistence.model.filterActive
import de.darkatra.vrising.discord.persistence.model.filterInactive
import dev.kord.core.Kord
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class ServerService(
    private val serverRepository: ServerRepository,
    private val statusMonitorService: StatusMonitorService,
    private val playerActivityFeedService: PlayerActivityFeedService,
    private val pvpKillFeedService: PvpKillFeedService
) {

    private val logger by lazy { LoggerFactory.getLogger(javaClass) }

    suspend fun updateServers(kord: Kord) {

        val activeServers = serverRepository.getServers().filterActive()
        activeServers.forEach { server ->

            MDC.put("server-id", server.id)

            updateStatusMonitor(kord, server)
            updatePlayerActivityFeed(kord, server)
            updatePvpKillFeed(kord, server)

            try {
                serverRepository.updateServer(server)
            } catch (e: OutdatedServerException) {
                logger.debug("Server was updated or deleted by another thread. Will ignore this exception and proceed with the next server.", e)
            }

            MDC.clear()
        }
    }

    fun cleanupInactiveServers() {

        val inactiveServers = serverRepository.getServers().filterInactive()
        if (inactiveServers.isEmpty()) {
            logger.info("No inactive servers to clean up.")
            return
        }

        val sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS)
        val serverIds = inactiveServers.map { server ->
            if (server.lastUpdated.isBefore(sevenDaysAgo)) {
                serverRepository.removeServer(server.id)
            }
            server.id
        }

        logger.info("Successfully removed ${serverIds.count()} servers with no active feature: $serverIds")
    }

    private suspend fun updateStatusMonitor(kord: Kord, server: Server) {

        val statusMonitor = server.statusMonitor
        if (statusMonitor == null || statusMonitor.status == Status.INACTIVE) {
            logger.debug("No active status monitor to update for server '${server.id}'.")
            return
        }

        statusMonitorService.updateStatusMonitor(kord, statusMonitor)
    }

    private suspend fun updatePlayerActivityFeed(kord: Kord, server: Server) {

        val playerActivityFeed = server.playerActivityFeed
        if (playerActivityFeed == null || playerActivityFeed.status == Status.INACTIVE) {
            logger.debug("No active player activity feed to update for server '${server.id}'.")
            return
        }

        playerActivityFeedService.updatePlayerActivityFeed(kord, playerActivityFeed)
    }

    private suspend fun updatePvpKillFeed(kord: Kord, server: Server) {

        val pvpKillFeed = server.pvpKillFeed
        if (pvpKillFeed == null || pvpKillFeed.status == Status.INACTIVE) {
            logger.debug("No active pvp kill feed to update for server '${server.id}'.")
            return
        }

        pvpKillFeedService.updatePvpKillFeed(kord, pvpKillFeed)
    }
}
