package de.darkatra.vrising.discord

import org.dizitart.no2.IndexType
import org.dizitart.no2.objects.Id
import org.dizitart.no2.objects.Index
import org.dizitart.no2.objects.Indices
import java.time.Instant

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
    val occurredAt: Instant
)

class ServerStatusMonitorBuilder(
    var id: String,
    var discordServerId: String,
    var discordChannelId: String,

    var hostName: String,
    var queryPort: Int,
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
            status = status,
            displayServerDescription = displayServerDescription,
            currentEmbedMessageId = currentEmbedMessageId,
            currentFailedAttempts = currentFailedAttempts,
            recentErrors = recentErrors
        )
    }
}
