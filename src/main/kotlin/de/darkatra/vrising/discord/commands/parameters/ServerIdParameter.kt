package de.darkatra.vrising.discord.commands.parameters

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string

object ServerIdParameter {
    const val NAME = "server-id"
}

fun GlobalChatInputCreateBuilder.addServerIdParameter() {
    string(
        name = ServerIdParameter.NAME,
        description = "The id of the server."
    ) {
        required = true
    }
}

fun ChatInputCommandInteraction.getServerIdParameter(): String {
    return command.strings[ServerIdParameter.NAME]!!
}
