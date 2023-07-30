package de.darkatra.vrising.discord.commands.parameters

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.integer

object ServerApiPortParameter {
    const val NAME = "server-api-port"
}

fun GlobalChatInputCreateBuilder.addServerApiPortParameter(required: Boolean = true) {
    integer(
        name = ServerApiPortParameter.NAME,
        description = "The api port of the server."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getServerApiPortParameter(): Int? {
    return command.integers[ServerApiPortParameter.NAME]?.let { Math.toIntExact(it) }
}
