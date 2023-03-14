package de.darkatra.vrising.discord

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.edit
import dev.kord.core.exception.EntityNotFoundException
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.modify.embed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.dizitart.kno2.filters.and
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectFilter
import org.dizitart.no2.objects.filters.ObjectFilters
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.coroutines.CoroutineContext

@Service
class ServerStatusMonitorService(
    database: Nitrite,
    private val serverQueryClient: ServerQueryClient,
    private val botProperties: BotProperties
) : CoroutineScope {

    private val logger = LoggerFactory.getLogger(javaClass)

    override val coroutineContext: CoroutineContext = Dispatchers.Default

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
            serverStatusMonitor.builder().also {
                it.status = ServerStatusMonitorStatus.INACTIVE
            }.build()
        )
    }

    suspend fun updateServerStatusMonitor(kord: Kord) {
        getServerStatusMonitors(status = ServerStatusMonitorStatus.ACTIVE).forEach { serverStatusMonitor ->
            runCatching {

                val channel = kord.getChannel(Snowflake(serverStatusMonitor.discordChannelId))
                if (channel == null || channel !is MessageChannelBehavior) {
                    logger.debug(
                        """Disabling server monitor '${serverStatusMonitor.id}' because the channel
                                |'${serverStatusMonitor.discordChannelId}' does not seem to exist""".trimMargin()
                    )
                    disableServerStatusMonitor(serverStatusMonitor)
                    return@forEach
                }

                val serverInfo = serverQueryClient.getServerInfo(serverStatusMonitor.hostName, serverStatusMonitor.queryPort)
                val players = serverQueryClient.getPlayerList(serverStatusMonitor.hostName, serverStatusMonitor.queryPort)
                val rules = serverQueryClient.getRules(serverStatusMonitor.hostName, serverStatusMonitor.queryPort)

                val embedCustomizer: (embedBuilder: EmbedBuilder) -> Unit = { embedBuilder ->
                    ServerStatusEmbed.buildEmbed(
                        serverInfo,
                        players,
                        rules,
                        serverStatusMonitor.displayServerDescription,
                        embedBuilder
                    )
                }

                val currentEmbedMessageId = serverStatusMonitor.currentEmbedMessageId
                if (currentEmbedMessageId != null) {
                    try {
                        channel.getMessage(Snowflake(currentEmbedMessageId))
                            .edit { embed(embedCustomizer) }

                        serverStatusMonitor.currentFailedAttempts = 0
                        putServerStatusMonitor(serverStatusMonitor)

                        logger.debug("Successfully updated the status of server monitor: ${serverStatusMonitor.id}")
                        return@forEach
                    } catch (e: EntityNotFoundException) {
                        serverStatusMonitor.currentEmbedMessageId = null
                    }
                }

                serverStatusMonitor.currentEmbedMessageId = channel.createEmbed(embedCustomizer).id.toString()
                serverStatusMonitor.currentFailedAttempts = 0
                putServerStatusMonitor(serverStatusMonitor)

                logger.debug("Successfully updated the status and persisted the embedId of server monitor: ${serverStatusMonitor.id}")

            }.onFailure { throwable ->

                logger.error("Exception while fetching the status of ${serverStatusMonitor.id}", throwable)
                serverStatusMonitor.currentFailedAttempts += 1
                putServerStatusMonitor(serverStatusMonitor)

                if (botProperties.maxFailedAttempts != 0 && serverStatusMonitor.currentFailedAttempts >= botProperties.maxFailedAttempts) {
                    logger.debug("Disabling server monitor '${serverStatusMonitor.id}' because it exceeded the max failed attempts.")
                    disableServerStatusMonitor(serverStatusMonitor)

                    val channel = kord.getChannel(Snowflake(serverStatusMonitor.discordChannelId))
                    if (channel == null || channel !is MessageChannelBehavior) {
                        return@forEach
                    }

                    channel.createMessage(
                        """Disabled server status monitor '${serverStatusMonitor.id}' because the server did not
                                |respond after ${botProperties.maxFailedAttempts} attempts.
                                |Please make sure the server is running and is accessible from the internet to use this bot.
                                |You can re-enable the server status monitor with the update-server command.""".trimMargin()
                    )

                    return@forEach
                }
            }
        }
    }
}
