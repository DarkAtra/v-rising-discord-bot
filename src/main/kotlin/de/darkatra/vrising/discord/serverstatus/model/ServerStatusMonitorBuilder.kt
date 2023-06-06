package de.darkatra.vrising.discord.serverstatus.model

class ServerStatusMonitorBuilder(
    var id: String,
    var discordServerId: String,
    var discordChannelId: String,

    var hostname: String,
    var queryPort: Int,
    var apiHostname: String? = null,
    var apiPort: Int? = null,
    var status: ServerStatusMonitorStatus,

    var displayServerDescription: Boolean,
    var displayPlayerGearLevel: Boolean,

    var currentEmbedMessageId: String? = null,
    var currentFailedAttempts: Int,

    var recentErrors: MutableList<Error>
) {

    fun build(): ServerStatusMonitor {
        return ServerStatusMonitor(
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
            recentErrors = recentErrors
        )
    }
}
