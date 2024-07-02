package de.darkatra.vrising.discord.commands.parameters

import de.darkatra.vrising.discord.getChannelIdFromStringParameter
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string

object ChannelIdParameter {
    const val NAME = "channel-id"
}

fun GlobalChatInputCreateBuilder.addChannelIdParameter(required: Boolean = true) {
    string(
        name = ChannelIdParameter.NAME,
        description = "The id of the channel to post to."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getChannelIdParameter(): String? {
    return command.getChannelIdFromStringParameter(ChannelIdParameter.NAME)
}
