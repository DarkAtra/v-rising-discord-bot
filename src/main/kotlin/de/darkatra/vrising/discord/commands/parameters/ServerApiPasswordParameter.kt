package de.darkatra.vrising.discord.commands.parameters

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string

object ServerApiPasswordParameter {
    const val NAME = "server-api-password"
}

fun GlobalChatInputCreateBuilder.addServerApiPasswordParameter(required: Boolean = true) {
    string(
        name = ServerApiPasswordParameter.NAME,
        description = "The password used to authenticate to the api of the server."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getServerApiPasswordParameter(): String? {
    return command.strings[ServerApiPasswordParameter.NAME]
}
