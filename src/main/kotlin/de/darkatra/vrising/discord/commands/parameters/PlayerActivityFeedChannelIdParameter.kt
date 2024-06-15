package de.darkatra.vrising.discord.commands.parameters

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.channel

object PlayerActivityFeedChannelIdParameter {
    const val NAME = "player-activity-feed-channel"
}

fun GlobalChatInputCreateBuilder.addPlayerActivityFeedChannelIdParameter(required: Boolean = true) {
    channel(
        name = PlayerActivityFeedChannelIdParameter.NAME,
        description = "The channel to post the player activity feed in."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getPlayerActivityFeedChannelIdParameter(): String? {
    return command.channels[PlayerActivityFeedChannelIdParameter.NAME]?.id?.toString()
}
