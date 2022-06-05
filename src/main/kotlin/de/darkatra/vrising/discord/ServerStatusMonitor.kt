package de.darkatra.vrising.discord

import org.dizitart.no2.objects.Id

data class ServerStatusMonitor(
    @Id
    val id: String,
    val hostName: String,
    val queryPort: Int,
    val discordChannelId: String,
    var currentEmbedMessageId: String? = null
)
