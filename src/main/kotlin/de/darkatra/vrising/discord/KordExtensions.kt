package de.darkatra.vrising.discord

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.interaction.InteractionCommand

class UnexpectedDiscordException(message: String, cause: Throwable? = null) : BotException(message, cause)
class InvalidDiscordChannelException(message: String) : BotException(message)

suspend fun Kord.getDiscordChannel(discordChannelId: String): Result<MessageChannelBehavior> {
    val channel = try {
        getChannel(Snowflake(discordChannelId))
    } catch (e: Exception) {
        return Result.failure(UnexpectedDiscordException("Exception getting the Discord Channel for '$discordChannelId'.", e))
    }
    if (channel == null || channel !is MessageChannelBehavior) {
        return Result.failure(InvalidDiscordChannelException("Discord Channel with id '$discordChannelId' does not exist."))
    }
    return Result.success(channel)
}

private val CHANNEL_PATTERN = Regex("^<#(\\d+)>").toPattern()

fun InteractionCommand.getChannelIdFromStringParameter(parameterName: String): String? {
    val value = strings[parameterName] ?: return null
    val matcher = CHANNEL_PATTERN.matcher(value)
    if (!matcher.find()) {
        return value
    }
    val result = matcher.toMatchResult()
    return result.group(1)
}

suspend fun MessageChannelBehavior.tryCreateMessage(message: String): Boolean {
    try {
        createMessage(message)
        return true
    } catch (e: Exception) {
        return false
    }
}
