package de.darkatra.vrising.discord.commands.parameters

import de.darkatra.vrising.discord.getChannelIdFromStringParameter
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string

object PvpKillFeedChannelIdParameter {
    const val NAME = "pvp-kill-feed-channel-id"
}

fun GlobalChatInputCreateBuilder.addPvpKillFeedChannelIdParameter(required: Boolean = true) {
    string(
        name = PvpKillFeedChannelIdParameter.NAME,
        description = "The id of the channel to post the pvp kill feed in."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getPvpKillFeedChannelIdParameter(): String? {
    return command.getChannelIdFromStringParameter(PvpKillFeedChannelIdParameter.NAME)
}
