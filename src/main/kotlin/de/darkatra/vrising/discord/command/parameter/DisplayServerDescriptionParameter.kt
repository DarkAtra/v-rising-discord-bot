package de.darkatra.vrising.discord.command.parameter

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.boolean

private const val PARAMETER_NAME = "display-server-description"

fun GlobalChatInputCreateBuilder.addDisplayServerDescriptionParameter(required: Boolean = true) {
    boolean(
        name = PARAMETER_NAME,
        description = "Whether or not to display the v rising server description on discord."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getDisplayServerDescriptionParameter(): Boolean? {
    return command.booleans[PARAMETER_NAME]
}
