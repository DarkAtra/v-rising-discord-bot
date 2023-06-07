package de.darkatra.vrising.discord.serverstatus.model

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

    val hostname: String,
    val queryPort: Int,
    val apiHostname: String? = null,
    val apiPort: Int? = null,
    val status: ServerStatusMonitorStatus,

    val displayServerDescription: Boolean,
    val displayPlayerGearLevel: Boolean,

    val currentEmbedMessageId: String? = null,
    val currentFailedAttempts: Int = 0,

    val recentErrors: List<Error> = emptyList()
) {

    val apiEnabled = apiHostname != null && apiPort != null

    fun builder(): ServerStatusMonitorBuilder {
        return ServerStatusMonitorBuilder(
            id = id,
            discordServerId = discordServerId,
            discordChannelId = discordChannelId,
            hostname = hostname,
            queryPort = queryPort,
            apiHostname = apiHostname,
            apiPort = apiPort,
            status = status,
            displayServerDescription = displayServerDescription,
            displayPlayerGearLevel = displayPlayerGearLevel,
            currentEmbedMessageId = currentEmbedMessageId,
            currentFailedAttempts = currentFailedAttempts,
            recentErrors = recentErrors.toMutableList()
        )
    }
}
