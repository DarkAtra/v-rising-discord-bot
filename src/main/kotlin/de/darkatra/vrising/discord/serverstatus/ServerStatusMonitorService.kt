package de.darkatra.vrising.discord.serverstatus

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.botcompanion.BotCompanionClient
import de.darkatra.vrising.discord.botcompanion.model.VBlood
import de.darkatra.vrising.discord.plus
import de.darkatra.vrising.discord.serverquery.ServerQueryClient
import de.darkatra.vrising.discord.serverstatus.model.Error
import de.darkatra.vrising.discord.serverstatus.model.Player
import de.darkatra.vrising.discord.serverstatus.model.ServerInfo
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
import org.dizitart.kno2.filters.and
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectFilter
import org.dizitart.no2.objects.filters.ObjectFilters
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ServerStatusMonitorService(
    database: Nitrite,
    private val serverQueryClient: ServerQueryClient,
    private val botCompanionClient: BotCompanionClient,
    private val botProperties: BotProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private var repository = database.getRepository(ServerStatusMonitor::class.java)

    fun putServerStatusMonitor(serverStatusMonitor: ServerStatusMonitor) {
        repository.update(serverStatusMonitor, true)
    }

    fun removeServerStatusMonitor(id: String, discordServerId: String): Boolean {
        return repository.remove(ObjectFilters.eq("id", id).and(ObjectFilters.eq("discordServerId", discordServerId))).affectedCount > 0
    }

    fun getServerStatusMonitor(id: String, discordServerId: String): ServerStatusMonitor? {
        return repository.find(ObjectFilters.eq("id", id).and(ObjectFilters.eq("discordServerId", discordServerId))).firstOrNull()
    }

    fun getServerStatusMonitors(discordServerId: String? = null, status: ServerStatusMonitorStatus? = null): List<ServerStatusMonitor> {

        var objectFilter: ObjectFilter? = null

        // apply filters
        if (discordServerId != null) {
            objectFilter = ObjectFilters.eq("discordServerId", discordServerId)
        }
        if (status != null) {
            objectFilter += ObjectFilters.eq("status", status)
        }

        return when {
            objectFilter != null -> repository.find(objectFilter).toList()
            else -> repository.find().toList()
        }
    }

    fun disableServerStatusMonitor(serverStatusMonitor: ServerStatusMonitor) {
        putServerStatusMonitor(
            serverStatusMonitor.builder().apply {
                status = ServerStatusMonitorStatus.INACTIVE
            }.build()
        )
    }

    suspend fun updateServerStatusMonitor(kord: Kord) {
        getServerStatusMonitors(status = ServerStatusMonitorStatus.ACTIVE).forEach { serverStatusMonitor ->
            updateServerStatusMonitor(kord, serverStatusMonitor)
        }
    }

    suspend fun updateServerStatusMonitor(kord: Kord, serverStatusMonitor: ServerStatusMonitor) {

        val serverStatusMonitorBuilder = serverStatusMonitor.builder()

        runCatching {

            val channel = kord.getChannel(Snowflake(serverStatusMonitor.discordChannelId))
            if (channel == null || channel !is MessageChannelBehavior) {
                logger.debug(
                    """Disabling server monitor '${serverStatusMonitor.id}' because the channel
                        |'${serverStatusMonitor.discordChannelId}' does not seem to exist""".trimMargin()
                )
                disableServerStatusMonitor(serverStatusMonitor)
                return
            }

            val serverInfo = serverQueryClient.getServerInfo(serverStatusMonitor.hostName, serverStatusMonitor.queryPort)
            val players = serverQueryClient.getPlayerList(serverStatusMonitor.hostName, serverStatusMonitor.queryPort)
            val rules = serverQueryClient.getRules(serverStatusMonitor.hostName, serverStatusMonitor.queryPort)
            val characters = when {
                serverStatusMonitor.apiPort != null -> botCompanionClient.getCharacters(serverStatusMonitor.hostName, serverStatusMonitor.apiPort)
                else -> emptyList()
            }

            val embedCustomizer: (embedBuilder: EmbedBuilder) -> Unit = { embedBuilder ->
                ServerStatusEmbed.buildEmbed(
                    ServerInfo(
                        name = serverInfo.name,
                        ip = serverInfo.hostAddress,
                        gamePort = serverInfo.gamePort,
                        queryPort = serverInfo.port,
                        numberOfPlayers = serverInfo.numOfPlayers,
                        maxPlayers = serverInfo.maxPlayers,
                        players = players.map { sourcePlayer ->
                            val character = characters.find { character -> character.name == sourcePlayer.name }
                            Player(
                                name = sourcePlayer.name,
                                gearLevel = character?.gearLevel,
                                clan = character?.clan,
                                killedVBloods = character?.killedVBloods?.map(VBlood::displayName)
                            )
                        },
                        rules = rules
                    ),
                    serverStatusMonitor.apiPort != null,
                    serverStatusMonitor.displayServerDescription,
                    serverStatusMonitor.displayClan,
                    serverStatusMonitor.displayGearLevel,
                    serverStatusMonitor.displayKilledVBloods,
                    embedBuilder
                )
            }

            val currentEmbedMessageId = serverStatusMonitor.currentEmbedMessageId
            if (currentEmbedMessageId != null) {
                try {
                    channel.getMessage(Snowflake(currentEmbedMessageId))
                        .edit { embed(embedCustomizer) }

                    serverStatusMonitorBuilder.currentFailedAttempts = 0
                    putServerStatusMonitor(serverStatusMonitorBuilder.build())

                    logger.debug("Successfully updated the status of server monitor: ${serverStatusMonitor.id}")
                    return
                } catch (e: EntityNotFoundException) {
                    serverStatusMonitorBuilder.currentEmbedMessageId = null
                }
            }

            serverStatusMonitorBuilder.currentEmbedMessageId = channel.createEmbed(embedCustomizer).id.toString()
            serverStatusMonitorBuilder.currentFailedAttempts = 0

            putServerStatusMonitor(serverStatusMonitorBuilder.build())

            logger.debug("Successfully updated the status and persisted the embedId of server monitor: ${serverStatusMonitor.id}")

        }.onFailure { throwable ->

            logger.error("Exception while fetching the status of ${serverStatusMonitor.id}", throwable)
            serverStatusMonitorBuilder.currentFailedAttempts += 1

            if (botProperties.maxRecentErrors > 0) {
                serverStatusMonitorBuilder.recentErrors = serverStatusMonitorBuilder.recentErrors
                    .takeLast((botProperties.maxRecentErrors - 1).coerceAtLeast(0))
                    .toMutableList()
                    .apply {
                        add(
                            Error(
                                message = "${throwable::class.simpleName}: ${throwable.message}",
                                timestamp = Instant.now().toString()
                            )
                        )
                    }
            }

            putServerStatusMonitor(serverStatusMonitorBuilder.build())

            if (botProperties.maxFailedAttempts != 0 && serverStatusMonitor.currentFailedAttempts >= botProperties.maxFailedAttempts) {
                logger.debug("Disabling server monitor '${serverStatusMonitor.id}' because it exceeded the max failed attempts.")
                disableServerStatusMonitor(serverStatusMonitor)

                val channel = kord.getChannel(Snowflake(serverStatusMonitor.discordChannelId))
                if (channel == null || channel !is MessageChannelBehavior) {
                    return
                }

                channel.createMessage(
                    """Disabled server status monitor '${serverStatusMonitor.id}' because the server did not
                        |respond after ${botProperties.maxFailedAttempts} attempts.
                        |Please make sure the server is running and is accessible from the internet to use this bot.
                        |You can re-enable the server status monitor with the update-server command.""".trimMargin()
                )

                return
            }
        }
    }
}
