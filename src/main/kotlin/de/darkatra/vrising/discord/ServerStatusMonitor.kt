package de.darkatra.vrising.discord

import org.dizitart.no2.IndexType
import org.dizitart.no2.objects.Id
import org.dizitart.no2.objects.Index

@Index(value = "discordServerId", type = IndexType.NonUnique)
data class ServerStatusMonitor(
    @Id
    val id: String,
    val hostName: String,
    val queryPort: Int,
    val discordServerId: String,
    val discordChannelId: String,
    var currentEmbedMessageId: String? = null
)
