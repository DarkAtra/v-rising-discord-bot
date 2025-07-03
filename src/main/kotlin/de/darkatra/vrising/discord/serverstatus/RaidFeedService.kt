package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.InvalidDiscordChannelException
import de.darkatra.vrising.discord.clients.botcompanion.BotCompanionClient
import de.darkatra.vrising.discord.clients.botcompanion.model.Raid
import de.darkatra.vrising.discord.clients.botcompanion.model.Raid.Player
import de.darkatra.vrising.discord.commands.ConfigureRaidFeedCommand
import de.darkatra.vrising.discord.getDiscordChannel
import de.darkatra.vrising.discord.persistence.model.RaidFeed
import de.darkatra.vrising.discord.persistence.model.Status
import de.darkatra.vrising.discord.toReadableString
import de.darkatra.vrising.discord.tryCreateMessage
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.MessageChannelBehavior
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RaidFeedService(
    private val botProperties: BotProperties,
    private val botCompanionClient: BotCompanionClient,
    private val configureRaidFeedCommand: ConfigureRaidFeedCommand
) {

    private val logger by lazy { LoggerFactory.getLogger(javaClass) }

    suspend fun updateRaidFeed(kord: Kord, raidFeed: RaidFeed) {

        if (!raidFeed.getServer().apiEnabled) {
            logger.debug("Skipping raid feed update for server '${raidFeed.getServer().id}' because apiEnabled is false.")
            return
        }

        logger.debug("Attempting to update the raid feed for server '${raidFeed.getServer().id}'...")

        val raidFeedChannel = kord.getDiscordChannel(raidFeed.discordChannelId).getOrElse { e ->
            when (e) {
                is InvalidDiscordChannelException -> {
                    logger.debug("Disabling raid feed for server '${raidFeed.getServer().id}' because the channel '${raidFeed.discordChannelId}' does not seem to exist.")
                    raidFeed.status = Status.INACTIVE
                }

                else -> {
                    raidFeed.currentFailedAttempts += 1
                    raidFeed.addError(e, botProperties.maxRecentErrors)
                    disableRaidFeedIfNecessary(raidFeed)
                }
            }
            return
        }

        val raids = botCompanionClient.getRaids(
            raidFeed.getServer().apiHostname!!,
            raidFeed.getServer().apiPort!!,
            raidFeed.getServer().apiUsername,
            raidFeed.getServer().apiPassword
        ).getOrElse { e ->

            logger.error("Exception updating the raid feed for server ${raidFeed.getServer().id}", e)
            raidFeed.currentFailedAttempts += 1
            raidFeed.addError(e, botProperties.maxRecentErrors)

            disableRaidFeedIfNecessary(raidFeed) {
                raidFeedChannel.tryCreateMessage(
                    """Disabled the raid feed for server '${raidFeed.getServer().id}' because
                        |the bot companion did not respond successfully after ${botProperties.maxFailedApiAttempts} attempts.
                        |Please make sure the server-api-hostname and server-api-port are correct.
                        |You can re-enable this functionality using the ${configureRaidFeedCommand.getCommandName()} command.""".trimMargin()
                )
            }
            return
        }

        raidFeed.currentFailedAttempts = 0

        raids
            .filter { raid -> raid.updated != null }
            .filter { raid -> raid.updated!!.isAfter(raidFeed.lastUpdated) }
            .filter { raid -> raid.attackers.isNotEmpty() && raid.defenders.isNotEmpty() }
            .sortedWith(Comparator.comparing(Raid::updated))
            .forEach { raid ->
                try {
                    postRaidFeedMessage(raid, raidFeedChannel, raidFeed)
                } catch (e: Exception) {
                    logger.warn("Could not post raid feed message for server '${raidFeed.getServer().id}'.", e)
                }
            }

        raidFeed.lastUpdated = Instant.now()

        logger.debug("Successfully updated the raid feed for server '${raidFeed.getServer().id}'.")
    }

    private suspend fun postRaidFeedMessage(raid: Raid, raidFeedChannel: MessageChannelBehavior, raidFeed: RaidFeed) {

        requireNotNull(raid.updated)

        val defendersString = raid.defenders.map { mapToPlayerString(it, raidFeed.displayPlayerGearLevel) }.toReadableString()

        // new raid
        if (raid.occurred == raid.updated) {
            val attackersString = raid.attackers.map { mapToPlayerString(it, raidFeed.displayPlayerGearLevel) }.toReadableString()
            val verb = when (raid.attackers.size) {
                1 -> "is"
                else -> "are"
            }
            raidFeedChannel.createMessage(
                "<t:${raid.updated.epochSecond}>: $attackersString $verb raiding $defendersString."
            )
            return
        }

        // players joined an ongoing raid
        val newAttackersString = raid.attackers
            .filter { player -> player.joinedAt!!.isAfter(raidFeed.lastUpdated) }
            .map { mapToPlayerString(it, raidFeed.displayPlayerGearLevel) }
            .toReadableString()
        raidFeedChannel.createMessage(
            "<t:${raid.updated.epochSecond}>: $newAttackersString joined the raid against $defendersString."
        )
    }

    private fun mapToPlayerString(player: Player, displayPlayerGearLevel: Boolean): String {
        return when {
            displayPlayerGearLevel -> "${player.name} (${player.gearLevel})"
            else -> player.name
        }
    }

    private suspend fun disableRaidFeedIfNecessary(raidFeed: RaidFeed, block: suspend () -> Unit = {}) {

        if (botProperties.maxFailedApiAttempts != 0 && raidFeed.currentFailedAttempts >= botProperties.maxFailedApiAttempts) {
            logger.warn("Disabling the raid feed for server '${raidFeed.getServer().id}' because it exceeded the max failed api attempts.")
            raidFeed.status = Status.INACTIVE
            block()
        }
    }
}
