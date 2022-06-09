package de.darkatra.vrising.discord

import org.dizitart.no2.IndexType
import org.dizitart.no2.objects.Id
import org.dizitart.no2.objects.Index

@Index(value = "discordServerId", type = IndexType.NonUnique)
data class ServerStatusMonitor(
    @Id
    val id: String,
    val discordServerId: String,
    val discordChannelId: String,

    val hostName: String,
    val queryPort: Int,
    val displayPlayerGearLevel: Boolean,

    var currentEmbedMessageId: String? = null,
) {

    fun builder(): ServerStatusMonitorBuilder {
        return ServerStatusMonitorBuilder(
            id = id,
            discordServerId = discordServerId,
            discordChannelId = discordChannelId,
            hostName = hostName,
            queryPort = queryPort,
            displayPlayerGearLevel = displayPlayerGearLevel,
            currentEmbedMessageId = currentEmbedMessageId
        )
    }
}

class ServerStatusMonitorBuilder(
    var id: String,
    var discordServerId: String,
    var discordChannelId: String,

    var hostName: String,
    var queryPort: Int,
    var displayPlayerGearLevel: Boolean,

    var currentEmbedMessageId: String? = null,
) {

    fun build(): ServerStatusMonitor {
        return ServerStatusMonitor(
            id = id,
            discordServerId = discordServerId,
            discordChannelId = discordChannelId,
            hostName = hostName,
            queryPort = queryPort,
            displayPlayerGearLevel = displayPlayerGearLevel,
            currentEmbedMessageId = currentEmbedMessageId
        )
    }
}
