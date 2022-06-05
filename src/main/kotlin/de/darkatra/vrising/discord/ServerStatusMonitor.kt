package de.darkatra.vrising.discord

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import org.kodein.db.model.Id

@Serializable
data class ServerStatusMonitor(
	@Id
	val id: String,
	val hostName: String,
	val queryPort: Int,
	val discordChannelId: Snowflake,
	var currentEmbedMessageId: Snowflake? = null
)
