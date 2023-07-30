package de.darkatra.vrising.discord.commands.parameters

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.integer

object ServerQueryPortParameter {
    const val NAME = "server-query-port"
}

fun GlobalChatInputCreateBuilder.addServerQueryPortParameter(required: Boolean = true) {
    integer(
        name = ServerQueryPortParameter.NAME,
        description = "The query port of the server to add a status monitor for."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getServerQueryPortParameter(): Int? {
    return command.integers[ServerQueryPortParameter.NAME]?.let { Math.toIntExact(it) }
}
