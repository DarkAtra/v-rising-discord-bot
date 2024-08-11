package de.darkatra.vrising.discord.clients.serverquery

import de.darkatra.vrising.discord.BotException

open class ServerQueryClientException(message: String, cause: Throwable? = null) : BotException(message, cause)

class CancellationException(message: String, cause: Throwable? = null) : ServerQueryClientException(message, cause)
