package de.darkatra.vrising.discord.command.parameter

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.boolean

private const val PARAMETER_NAME = "display-player-gear-level"

fun GlobalChatInputCreateBuilder.addDisplayPlayerGearLevelParameter(required: Boolean = true) {
    boolean(
        name = PARAMETER_NAME,
        description = "Whether or not to display the gear level in the player list."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getDisplayPlayerGearLevelParameter(): Boolean? {
    return command.booleans[PARAMETER_NAME]
}
