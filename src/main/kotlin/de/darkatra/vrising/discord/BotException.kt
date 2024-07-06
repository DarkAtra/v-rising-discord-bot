package de.darkatra.vrising.discord

sealed class BotException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
