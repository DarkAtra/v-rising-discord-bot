package de.darkatra.vrising.discord.commands.parameters

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string

object DiscordServerIdParameter {
    const val NAME = "discord-server-id"
}

fun GlobalChatInputCreateBuilder.addDiscordServerIdParameter(required: Boolean = true) {
    string(
        name = DiscordServerIdParameter.NAME,
        description = "The id of the discord server."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getDiscordServerIdParameter(): String? {
    return command.strings[DiscordServerIdParameter.NAME]
}
