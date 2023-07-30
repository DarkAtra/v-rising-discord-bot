package de.darkatra.vrising.discord.serverstatus.exceptions

import de.darkatra.vrising.discord.BotException

class InvalidDiscordChannelException(message: String, val discordChannelId: String) : BotException(message)
