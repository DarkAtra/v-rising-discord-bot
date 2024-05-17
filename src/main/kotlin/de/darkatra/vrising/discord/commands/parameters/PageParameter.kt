package de.darkatra.vrising.discord.commands.parameters

import de.darkatra.vrising.discord.commands.exceptions.ValidationException
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.integer

object PageParameter {
    const val NAME = "page"

    fun validate(page: Int) {
        if (page < 0) {
            throw ValidationException("'$NAME' must be greater than or equal to zero. Rejected: $page")
        }
    }
}

fun GlobalChatInputCreateBuilder.addPageParameter(required: Boolean = true) {
    integer(
        name = PageParameter.NAME,
        description = "The page. Defaults to 0, the first page."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getPageParameter(): Int? {
    return command.integers[PageParameter.NAME]?.let { Math.toIntExact(it) }
}
