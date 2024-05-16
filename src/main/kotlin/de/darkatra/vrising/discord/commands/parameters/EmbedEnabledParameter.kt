package de.darkatra.vrising.discord.commands.parameters

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.boolean

object EmbedEnabledParameter {
    const val NAME = "embed-enabled"
}

fun GlobalChatInputCreateBuilder.addEmbedEnabledParameter(required: Boolean = true) {
    boolean(
        name = EmbedEnabledParameter.NAME,
        description = "Whether or not a discord status embed should be posted. Defaults to true."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getEmbedEnabledParameter(): Boolean? {
    return command.booleans[EmbedEnabledParameter.NAME]
}
