package de.darkatra.vrising.discord.commands.parameters

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.boolean

object DisplayServerDescriptionParameter {
    const val NAME = "display-server-description"
}

fun GlobalChatInputCreateBuilder.addDisplayServerDescriptionParameter(required: Boolean = true) {
    boolean(
        name = DisplayServerDescriptionParameter.NAME,
        description = "Whether or not to display the v rising server description on discord. Defaults to true."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getDisplayServerDescriptionParameter(): Boolean? {
    return command.booleans[DisplayServerDescriptionParameter.NAME]
}
