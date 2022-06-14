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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.dizitart.kno2.filters.and
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectFilter
import org.dizitart.no2.objects.filters.ObjectFilters
import org.springframework.stereotype.Service
import java.time.Duration
import kotlin.coroutines.CoroutineContext

@Service
class ServerStatusMonitorService(
    database: Nitrite,
    private val serverQueryClient: ServerQueryClient
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Default

    private var repository = database.getRepository(ServerStatusMonitor::class.java)

    fun putServerStatusMonitor(serverStatusMonitor: ServerStatusMonitor) {
        if (repository.find(ObjectFilters.eq("id", serverStatusMonitor.id)).firstOrNull() != null) {
            repository.update(serverStatusMonitor)
        } else {
            repository.insert(serverStatusMonitor)
        }
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

    fun launchServerStatusMonitor(kord: Kord) {
        launch {
            while (isActive) {
                runCatching {
                    getServerStatusMonitors(status = ServerStatusMonitorStatus.ACTIVE).forEach { serverStatusConfiguration ->

                        val channel = kord.getChannel(Snowflake(serverStatusConfiguration.discordChannelId))
                        if (channel == null || channel !is MessageChannelBehavior) {
                            return@forEach
                        }

                        val serverInfo = serverQueryClient.getServerInfo(serverStatusConfiguration.hostName, serverStatusConfiguration.queryPort)
                        val players = serverQueryClient.getPlayerList(serverStatusConfiguration.hostName, serverStatusConfiguration.queryPort)
                        val rules = serverQueryClient.getRules(serverStatusConfiguration.hostName, serverStatusConfiguration.queryPort)

                        val embedCustomizer: (embedBuilder: EmbedBuilder) -> Unit = { embedBuilder ->
                            ServerStatusEmbed.buildEmbed(serverInfo,
                                players,
                                rules,
                                serverStatusConfiguration.displayPlayerGearLevel,
                                serverStatusConfiguration.displayServerDescription,
                                embedBuilder
                            )
                        }

                        val currentEmbedMessageId = serverStatusConfiguration.currentEmbedMessageId
                        if (currentEmbedMessageId != null) {
                            try {
                                channel.getMessage(Snowflake(currentEmbedMessageId))
                                    .edit { embed(embedCustomizer) }
                                return@forEach
                            } catch (e: EntityNotFoundException) {
                                serverStatusConfiguration.currentEmbedMessageId = null
                            }
                        }

                        serverStatusConfiguration.currentEmbedMessageId = channel.createEmbed(embedCustomizer).id.toString()
                        putServerStatusMonitor(serverStatusConfiguration)
                    }
                }.onFailure { throwable ->
                    println("Exception in status monitoring thread: ${throwable.message}")
                    throwable.printStackTrace()
                }

                delay(Duration.ofMinutes(1).toMillis())
            }
        }
    }
}
