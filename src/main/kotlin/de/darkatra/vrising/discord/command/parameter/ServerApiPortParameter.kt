package de.darkatra.vrising.discord.command.parameter

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.integer

private const val PARAMETER_NAME = "server-api-port"

fun GlobalChatInputCreateBuilder.addServerApiPortParameter(required: Boolean = true) {
    integer(
        name = PARAMETER_NAME,
        description = "The api port of the server."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getServerApiPortParameter(): Int? {
    return command.integers[PARAMETER_NAME]?.let { Math.toIntExact(it) }
}
