package de.darkatra.vrising.discord.commands.parameters

import de.darkatra.vrising.discord.getChannelIdFromStringParameter
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string

object PlayerActivityFeedChannelIdParameter {
    const val NAME = "player-activity-feed-channel-id"
}

fun GlobalChatInputCreateBuilder.addPlayerActivityFeedChannelIdParameter(required: Boolean = true) {
    string(
        name = PlayerActivityFeedChannelIdParameter.NAME,
        description = "The id of the channel to post the player activity feed in."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getPlayerActivityFeedChannelIdParameter(): String? {
    return command.getChannelIdFromStringParameter(PlayerActivityFeedChannelIdParameter.NAME)
}
