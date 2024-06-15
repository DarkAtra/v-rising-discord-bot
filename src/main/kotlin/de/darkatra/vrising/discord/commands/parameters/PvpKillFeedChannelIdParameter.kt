package de.darkatra.vrising.discord.commands.parameters

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.channel

object PvpKillFeedChannelIdParameter {
    const val NAME = "pvp-kill-feed-channel"
}

fun GlobalChatInputCreateBuilder.addPvpKillFeedChannelIdParameter(required: Boolean = true) {
    channel(
        name = PvpKillFeedChannelIdParameter.NAME,
        description = "The channel to post the pvp kill feed in."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getPvpKillFeedChannelIdParameter(): String? {
    return command.channels[PvpKillFeedChannelIdParameter.NAME]?.id?.toString()
}
