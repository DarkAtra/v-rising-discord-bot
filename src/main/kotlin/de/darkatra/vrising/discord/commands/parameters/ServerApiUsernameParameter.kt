package de.darkatra.vrising.discord.commands.parameters

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string

object ServerApiUsernameParameter {
    const val NAME = "server-api-username"
}

fun GlobalChatInputCreateBuilder.addServerApiUsernameParameter(required: Boolean = true) {
    string(
        name = ServerApiUsernameParameter.NAME,
        description = "The username used to authenticate to the api of the server."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getServerApiUsernameParameter(): String? {
    return command.strings[ServerApiUsernameParameter.NAME]
}
