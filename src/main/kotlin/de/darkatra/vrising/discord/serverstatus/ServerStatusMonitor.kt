package de.darkatra.vrising.discord.serverstatus

import org.dizitart.no2.IndexType
import org.dizitart.no2.objects.Id
import org.dizitart.no2.objects.Index
import org.dizitart.no2.objects.Indices

@Indices(
    value = [
        Index(value = "discordServerId", type = IndexType.NonUnique),
        Index(value = "status", type = IndexType.NonUnique)
    ]
)
data class ServerStatusMonitor(
    @Id
    val id: String,
    val discordServerId: String,
    val discordChannelId: String,

    val hostName: String,
    val queryPort: Int,
    val apiPort: Int? = null,
    val status: ServerStatusMonitorStatus,
    val displayServerDescription: Boolean,

    val currentEmbedMessageId: String? = null,
    val currentFailedAttempts: Int = 0,

    val recentErrors: List<Error> = emptyList()
) {

    fun builder(): ServerStatusMonitorBuilder {
        return ServerStatusMonitorBuilder(
            id = id,
            discordServerId = discordServerId,
            discordChannelId = discordChannelId,
            hostName = hostName,
            queryPort = queryPort,
            apiPort = apiPort,
            status = status,
            displayServerDescription = displayServerDescription,
            currentEmbedMessageId = currentEmbedMessageId,
            currentFailedAttempts = currentFailedAttempts,
            recentErrors = recentErrors.toMutableList()
        )
    }
}

data class Error(
    val message: String,
    val timestamp: String
)

class ServerStatusMonitorBuilder(
    var id: String,
    var discordServerId: String,
    var discordChannelId: String,

    var hostName: String,
    var queryPort: Int,
    var apiPort: Int? = null,
    var status: ServerStatusMonitorStatus,
    var displayServerDescription: Boolean,

    var currentEmbedMessageId: String? = null,
    var currentFailedAttempts: Int,

    var recentErrors: MutableList<Error>
) {

    fun build(): ServerStatusMonitor {
        return ServerStatusMonitor(
            id = id,
            discordServerId = discordServerId,
            discordChannelId = discordChannelId,
            hostName = hostName,
            queryPort = queryPort,
            apiPort = apiPort,
            status = status,
            displayServerDescription = displayServerDescription,
            currentEmbedMessageId = currentEmbedMessageId,
            currentFailedAttempts = currentFailedAttempts,
            recentErrors = recentErrors
        )
    }
}
