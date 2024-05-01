package de.darkatra.vrising.discord.serverstatus.model

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
    @Deprecated("This field is updated automatically by the ServerStatusMonitorRepository, manually update with caution")
    var version: Long? = null,

    var discordServerId: String,
    var discordChannelId: String,
    var playerActivityDiscordChannelId: String? = null,
    var pvpKillFeedDiscordChannelId: String? = null,

    var hostname: String,
    var queryPort: Int,

    var apiHostname: String? = null,
    var apiPort: Int? = null,
    var apiUsername: String? = null,
    var apiPassword: String? = null,

    var status: ServerStatusMonitorStatus,

    var displayServerDescription: Boolean,
    var displayPlayerGearLevel: Boolean,

    var currentEmbedMessageId: String? = null,
    var currentFailedAttempts: Int = 0,

    var recentErrors: List<Error> = emptyList()
) {

    val apiEnabled: Boolean
        get() = apiHostname != null && apiPort != null

    @Suppress("DEPRECATION") // this is the internal usage the warning is referring to
    val lastUpdated: Instant
        get() = Instant.ofEpochMilli(version!!)
}
