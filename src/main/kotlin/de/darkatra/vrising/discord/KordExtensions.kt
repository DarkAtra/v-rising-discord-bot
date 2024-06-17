package de.darkatra.vrising.discord

import de.darkatra.vrising.discord.serverstatus.exceptions.InvalidDiscordChannelException
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.MessageChannelBehavior

suspend fun Kord.getDiscordChannel(discordChannelId: String): Result<MessageChannelBehavior> {
    val channel = try {
        getChannel(Snowflake(discordChannelId))
    } catch (e: Exception) {
        return Result.failure(InvalidDiscordChannelException("Exception getting the Discord Channel for '$discordChannelId'.", e))
    }
    if (channel == null || channel !is MessageChannelBehavior) {
        return Result.failure(InvalidDiscordChannelException("Discord Channel '$discordChannelId' does not exist."))
    }
    return Result.success(channel)
}
