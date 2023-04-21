package de.darkatra.vrising.discord.command.parameter

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.integer

private const val PARAMETER_NAME = "server-query-port"

fun GlobalChatInputCreateBuilder.addServerQueryPortParameter(required: Boolean = true) {
    integer(
        name = PARAMETER_NAME,
        description = "The query port of the server to add a status monitor for."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getServerQueryPortParameter(): Int? {
    return command.integers[PARAMETER_NAME]?.let { Math.toIntExact(it) }
}
