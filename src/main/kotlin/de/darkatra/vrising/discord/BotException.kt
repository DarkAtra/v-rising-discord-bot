package de.darkatra.vrising.discord

abstract class BotException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
