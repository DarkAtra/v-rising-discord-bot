package de.darkatra.vrising.discord.serverstatus.exceptions

import de.darkatra.vrising.discord.BotException

class InvalidDiscordChannelException(message: String, cause: Throwable? = null) : BotException(message, cause)
