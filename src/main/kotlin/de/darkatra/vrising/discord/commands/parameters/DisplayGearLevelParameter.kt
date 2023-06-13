package de.darkatra.vrising.discord.commands.parameters

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.boolean

object DisplayGearLevelParameter {
    const val NAME = "display-player-gear-level"
}

fun GlobalChatInputCreateBuilder.addDisplayPlayerGearLevelParameter(required: Boolean = true) {
    boolean(
        name = DisplayGearLevelParameter.NAME,
        description = "Whether or not to display each player's gear level. Defaults to true."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getDisplayPlayerGearLevelParameter(): Boolean? {
    return command.booleans[DisplayGearLevelParameter.NAME]
}
