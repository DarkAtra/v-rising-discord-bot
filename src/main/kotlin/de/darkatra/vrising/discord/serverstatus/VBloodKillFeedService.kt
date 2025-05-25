package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.InvalidDiscordChannelException
import de.darkatra.vrising.discord.clients.botcompanion.BotCompanionClient
import de.darkatra.vrising.discord.clients.botcompanion.model.VBloodKill
import de.darkatra.vrising.discord.commands.ConfigureVBloodKillFeedCommand
import de.darkatra.vrising.discord.getDiscordChannel
import de.darkatra.vrising.discord.persistence.model.Status
import de.darkatra.vrising.discord.persistence.model.VBloodKillFeed
import de.darkatra.vrising.discord.toReadableString
import de.darkatra.vrising.discord.tryCreateMessage
import dev.kord.core.Kord
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class VBloodKillFeedService(
    private val botProperties: BotProperties,
    private val botCompanionClient: BotCompanionClient,
    private val configureVBloodKillFeedCommand: ConfigureVBloodKillFeedCommand
) {

    private val logger by lazy { LoggerFactory.getLogger(javaClass) }

    suspend fun updateVBloodKillFeed(kord: Kord, vBloodKillFeed: VBloodKillFeed) {

        if (!vBloodKillFeed.getServer().apiEnabled) {
            logger.debug("Skipping vblood kill feed update for server '${vBloodKillFeed.getServer().id}' because apiEnabled is false.")
            return
        }

        logger.debug("Attempting to update the vblood kill feed for server '${vBloodKillFeed.getServer().id}'...")

        val vBloodKillFeedChannel = kord.getDiscordChannel(vBloodKillFeed.discordChannelId).getOrElse { e ->
            when (e) {
                is InvalidDiscordChannelException -> {
                    logger.debug("Disabling vblood kill feed for server '${vBloodKillFeed.getServer().id}' because the channel '${vBloodKillFeed.discordChannelId}' does not seem to exist.")
                    vBloodKillFeed.status = Status.INACTIVE
                }

                else -> {
                    vBloodKillFeed.currentFailedAttempts += 1
                    vBloodKillFeed.addError(e, botProperties.maxRecentErrors)
                    disableVBloodKillFeedIfNecessary(vBloodKillFeed)
                }
            }
            return
        }

        val vBloodKills = botCompanionClient.getVBloodKills(
            vBloodKillFeed.getServer().apiHostname!!,
            vBloodKillFeed.getServer().apiPort!!,
            vBloodKillFeed.getServer().apiUsername,
            vBloodKillFeed.getServer().apiPassword
        ).getOrElse { e ->

            logger.error("Exception updating the vblood kill feed for server ${vBloodKillFeed.getServer().id}", e)
            vBloodKillFeed.currentFailedAttempts += 1
            vBloodKillFeed.addError(e, botProperties.maxRecentErrors)

            disableVBloodKillFeedIfNecessary(vBloodKillFeed) {
                vBloodKillFeedChannel.tryCreateMessage(
                    """Disabled the vblood kill feed for server '${vBloodKillFeed.getServer().id}' because
                        |the bot companion did not respond successfully after ${botProperties.maxFailedApiAttempts} attempts.
                        |Please make sure the server-api-hostname and server-api-port are correct.
                        |You can re-enable this functionality using the ${configureVBloodKillFeedCommand.getCommandName()} command.""".trimMargin()
                )
            }
            return
        }

        vBloodKillFeed.currentFailedAttempts = 0

        vBloodKills
            .filter { vBloodKill -> vBloodKill.occurred.isAfter(vBloodKillFeed.lastUpdated) }
            .filter { vBloodKill -> vBloodKill.killers.isNotEmpty() }
            .sortedWith(Comparator.comparing(VBloodKill::occurred))
            .forEach { vBloodKill ->
                try {
                    vBloodKillFeedChannel.createMessage(
                        "<t:${vBloodKill.occurred.epochSecond}>: ${vBloodKill.killers.map { it.name }.toReadableString()} killed ${vBloodKill.vBlood.name}."
                    )
                } catch (e: Exception) {
                    logger.warn("Could not post vblood kill feed message for server '${vBloodKillFeed.getServer().id}'.", e)
                }
            }

        vBloodKillFeed.lastUpdated = Instant.now()

        logger.debug("Successfully updated the vblood kill feed for server '${vBloodKillFeed.getServer().id}'.")
    }

    private suspend fun disableVBloodKillFeedIfNecessary(vBloodKillFeed: VBloodKillFeed, block: suspend () -> Unit = {}) {

        if (botProperties.maxFailedApiAttempts != 0 && vBloodKillFeed.currentFailedAttempts >= botProperties.maxFailedApiAttempts) {
            logger.warn("Disabling the vblood kill feed for server '${vBloodKillFeed.getServer().id}' because it exceeded the max failed api attempts.")
            vBloodKillFeed.status = Status.INACTIVE
            block()
        }
    }
}
