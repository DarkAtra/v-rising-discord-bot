package de.darkatra.vrising.discord.commands

import de.darkatra.vrising.discord.persistence.model.ErrorAware
import dev.kord.rest.builder.message.EmbedBuilder
import org.springframework.util.StringUtils

fun EmbedBuilder.renderRecentErrors(errorAware: ErrorAware, maxCharactersPerError: Int) {
    val recentErrors = errorAware.recentErrors
    if (recentErrors.isNotEmpty()) {
        recentErrors.chunked(5).forEachIndexed { i, chunk ->
            field {
                name = "Errors - Page ${i + 1}"
                value = chunk.joinToString("\n") {
                    "<t:${it.timestamp.epochSecond}:R>```${StringUtils.truncate(it.message, maxCharactersPerError)}```"
                }
            }
        }
    }
}
