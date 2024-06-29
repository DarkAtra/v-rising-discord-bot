package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.clients.botcompanion.BotCompanionClient
import de.darkatra.vrising.discord.clients.botcompanion.model.PvpKill
import de.darkatra.vrising.discord.getDiscordChannel
import de.darkatra.vrising.discord.persistence.model.PvpKillFeed
import de.darkatra.vrising.discord.persistence.model.Status
import de.darkatra.vrising.discord.tryCreateMessage
import dev.kord.core.Kord
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class PvpKillFeedService(
    private val botProperties: BotProperties,
    private val botCompanionClient: BotCompanionClient
) {

    private val logger by lazy { LoggerFactory.getLogger(javaClass) }

    suspend fun updatePvpKillFeed(kord: Kord, pvpKillFeed: PvpKillFeed) {

        if (!pvpKillFeed.getServer().apiEnabled) {
            logger.debug("Skipping pvp kill feed update for server '${pvpKillFeed.getServer().id}' because apiEnabled is false.")
            return
        }

        val pvpKillFeedChannel = kord.getDiscordChannel(pvpKillFeed.discordChannelId).getOrElse {
            logger.debug("Disabling pvp kill feed for server '${pvpKillFeed.getServer().id}' because the channel '${pvpKillFeed.discordChannelId}' does not seem to exist.")
            pvpKillFeed.status = Status.INACTIVE
            return
        }

        val pvpKills = botCompanionClient.getPvpKills(
            pvpKillFeed.getServer().apiHostname!!,
            pvpKillFeed.getServer().apiPort!!,
            pvpKillFeed.getServer().apiUsername,
            pvpKillFeed.getServer().apiPassword
        ).getOrElse { e ->

            logger.error("Exception updating the pvp kill feed for server ${pvpKillFeed.getServer().id}", e)
            pvpKillFeed.currentFailedAttempts += 1

            if (botProperties.maxRecentErrors > 0) {
                pvpKillFeed.addError(e, botProperties.maxRecentErrors)
            }

            if (botProperties.maxFailedApiAttempts != 0 && pvpKillFeed.currentFailedAttempts >= botProperties.maxFailedApiAttempts) {
                logger.warn("Disabling the pvp kill feed for server '${pvpKillFeed.getServer().id}' because it exceeded the max failed api attempts.")
                pvpKillFeed.status = Status.INACTIVE

                // FIXME: mention the correct command to re-enable the player activity feed
                pvpKillFeedChannel.tryCreateMessage(
                    """Disabled the pvp kill feed for server '${pvpKillFeed.getServer().id}' because
                        |the bot companion did not respond successfully after ${botProperties.maxFailedApiAttempts} attempts.
                        |Please make sure the server-api-hostname and server-api-port are correct.
                        |You can re-enable the functionality using the update-server command.""".trimMargin()
                )
            }
            return
        }

        pvpKillFeed.currentFailedAttempts = 0

        pvpKills
            .filter { pvpKill -> pvpKill.occurred.isAfter(pvpKillFeed.lastUpdated) }
            .sortedWith(Comparator.comparing(PvpKill::occurred))
            .forEach { pvpKill ->
                pvpKillFeedChannel.tryCreateMessage(
                    "<t:${pvpKill.occurred.epochSecond}>: ${pvpKill.killer.name} (${pvpKill.killer.gearLevel}) killed ${pvpKill.victim.name} (${pvpKill.victim.gearLevel})."
                )
            }

        pvpKillFeed.lastUpdated = Instant.now()

        logger.debug("Successfully updated the pvp kill feed for server '${pvpKillFeed.getServer().id}'.")
    }
}
