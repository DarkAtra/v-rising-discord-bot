package de.darkatra.vrising.discord

import de.darkatra.vrising.discord.serverstatus.exceptions.InvalidDiscordChannelException
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.MessageChannelBehavior

suspend fun Kord.getDiscordChannel(discordChannelId: String): Result<MessageChannelBehavior> {
    val channel = getChannel(Snowflake(discordChannelId))
    if (channel == null || channel !is MessageChannelBehavior) {
        return Result.failure(InvalidDiscordChannelException("Discord Channel '$discordChannelId' does not exist.", discordChannelId))
    }
    return Result.success(channel)
}
