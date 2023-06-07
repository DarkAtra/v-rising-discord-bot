package de.darkatra.vrising.discord.serverstatus

class InvalidDiscordChannelException(message: String, val discordChannelId: String) : RuntimeException(message)
