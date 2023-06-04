package de.darkatra.vrising.discord.serverstatus.model

class ServerStatusMonitorBuilder(
    var id: String,
    var discordServerId: String,
    var discordChannelId: String,

    var hostName: String,
    var queryPort: Int,
    var apiPort: Int? = null,
    var status: ServerStatusMonitorStatus,

    var displayPlayersAsAsciiTable: Boolean,
    var displayServerDescription: Boolean,
    var displayClan: Boolean,
    var displayGearLevel: Boolean,
    var displayKilledVBloods: Boolean,

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
            displayPlayersAsAsciiTable = displayPlayersAsAsciiTable,
            displayServerDescription = displayServerDescription,
            displayClan = displayClan,
            displayGearLevel = displayGearLevel,
            displayKilledVBloods = displayKilledVBloods,
            currentEmbedMessageId = currentEmbedMessageId,
            currentFailedAttempts = currentFailedAttempts,
            recentErrors = recentErrors
        )
    }
}
