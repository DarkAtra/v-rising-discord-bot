package de.darkatra.vrising.discord.command.parameter

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.boolean

private const val PARAMETER_NAME = "display-gear-level"

fun GlobalChatInputCreateBuilder.addDisplayGearLevelParameter(required: Boolean = true) {
    boolean(
        name = PARAMETER_NAME,
        description = "Whether or not to display the gear level for each player. Defaults to true."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getDisplayGearLevelParameter(): Boolean? {
    return command.booleans[PARAMETER_NAME]
}
