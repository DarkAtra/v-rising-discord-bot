package de.darkatra.vrising.discord.command.parameter

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string

private const val PARAMETER_NAME = "server-hostname"

fun GlobalChatInputCreateBuilder.addServerHostnameParameter(required: Boolean = true) {
    string(
        name = PARAMETER_NAME,
        description = "The hostname of the server to add a status monitor for."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getServerHostnameParameter(): String? {
    return command.strings[PARAMETER_NAME]
}
