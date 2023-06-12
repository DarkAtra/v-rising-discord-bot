package de.darkatra.vrising.discord.command.parameter

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string

private const val PARAMETER_NAME = "player-activity-feed-channel-id"

fun GlobalChatInputCreateBuilder.addPlayerActivityFeedChannelIdParameter(required: Boolean = true) {
    string(
        name = PARAMETER_NAME,
        description = "The id of the channel to post the player activity feed in."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getPlayerActivityFeedChannelIdParameter(): String? {
    return command.strings[PARAMETER_NAME]
}
