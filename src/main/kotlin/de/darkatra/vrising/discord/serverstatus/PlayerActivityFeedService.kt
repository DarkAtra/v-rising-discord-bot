package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.InvalidDiscordChannelException
import de.darkatra.vrising.discord.clients.botcompanion.BotCompanionClient
import de.darkatra.vrising.discord.clients.botcompanion.model.PlayerActivity
import de.darkatra.vrising.discord.commands.ConfigurePlayerActivityFeedCommand
import de.darkatra.vrising.discord.getDiscordChannel
import de.darkatra.vrising.discord.persistence.model.PlayerActivityFeed
import de.darkatra.vrising.discord.persistence.model.Status
import de.darkatra.vrising.discord.tryCreateMessage
import dev.kord.core.Kord
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class PlayerActivityFeedService(
    private val botProperties: BotProperties,
    private val botCompanionClient: BotCompanionClient,
    private val configurePlayerActivityFeedCommand: ConfigurePlayerActivityFeedCommand
) {

    private val logger by lazy { LoggerFactory.getLogger(javaClass) }

    suspend fun updatePlayerActivityFeed(kord: Kord, playerActivityFeed: PlayerActivityFeed) {

        if (!playerActivityFeed.getServer().apiEnabled) {
            logger.debug("Skipping player activity feed update for server '${playerActivityFeed.getServer().id}' because apiEnabled is false.")
            return
        }

        val playerActivityChannel = kord.getDiscordChannel(playerActivityFeed.discordChannelId).getOrElse { e ->
            when (e) {
                is InvalidDiscordChannelException -> {
                    logger.debug("Disabling player activity feed for server '${playerActivityFeed.getServer().id}' because the channel '${playerActivityFeed.discordChannelId}' does not seem to exist.")
                    playerActivityFeed.status = Status.INACTIVE
                }

                else -> {
                    playerActivityFeed.currentFailedAttempts += 1
                    playerActivityFeed.addError(e, botProperties.maxRecentErrors)
                    disablePlayerActivityFeedIfNecessary(playerActivityFeed)
                }
            }
            return
        }

        val playerActivities = botCompanionClient.getPlayerActivities(
            playerActivityFeed.getServer().apiHostname!!,
            playerActivityFeed.getServer().apiPort!!,
            playerActivityFeed.getServer().apiUsername,
            playerActivityFeed.getServer().apiPassword
        ).getOrElse { e ->

            logger.error("Exception updating the player activity feed for server '${playerActivityFeed.getServer().id}'", e)
            playerActivityFeed.currentFailedAttempts += 1
            playerActivityFeed.addError(e, botProperties.maxRecentErrors)

            disablePlayerActivityFeedIfNecessary(playerActivityFeed) {
                playerActivityChannel.tryCreateMessage(
                    """Disabled the player activity feed for server '${playerActivityFeed.getServer().id}' because
                        |the bot companion did not respond successfully after ${botProperties.maxFailedApiAttempts} attempts.
                        |Please make sure the server-api-hostname and server-api-port are correct.
                        |You can re-enable this functionality using the ${configurePlayerActivityFeedCommand.getCommandName()} command.""".trimMargin()
                )
            }

            return
        }

        playerActivityFeed.currentFailedAttempts = 0

        playerActivities
            .filter { playerActivity -> playerActivity.occurred.isAfter(playerActivityFeed.lastUpdated) }
            .sortedWith(Comparator.comparing(PlayerActivity::occurred))
            .forEach { playerActivity ->
                val action = when (playerActivity.type) {
                    PlayerActivity.Type.CONNECTED -> "joined"
                    PlayerActivity.Type.DISCONNECTED -> "left"
                }
                playerActivityChannel.tryCreateMessage(
                    "<t:${playerActivity.occurred.epochSecond}>: ${playerActivity.playerName} $action the server."
                )
            }

        playerActivityFeed.lastUpdated = Instant.now()

        logger.debug("Successfully updated the player activity feed for server '${playerActivityFeed.getServer().id}'.")
    }

    private suspend fun disablePlayerActivityFeedIfNecessary(playerActivityFeed: PlayerActivityFeed, block: suspend () -> Unit = {}) {

        if (botProperties.maxFailedApiAttempts != 0 && playerActivityFeed.currentFailedAttempts >= botProperties.maxFailedApiAttempts) {
            logger.warn("Disabling the player activity feed for server '${playerActivityFeed.getServer().id}' because it exceeded the max failed api attempts.")
            playerActivityFeed.status = Status.INACTIVE
            block()
        }
    }
}
