package de.darkatra.vrising.discord.commands.parameters

import de.darkatra.vrising.discord.persistence.model.Status
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string

object StatusParameter {
    const val NAME = "status"
}

fun GlobalChatInputCreateBuilder.addStatusParameter(required: Boolean = true) {
    string(
        name = StatusParameter.NAME,
        description = "Determines if a feature is active or not."
    ) {
        this.required = required

        choice("ACTIVE", "ACTIVE")
        choice("INACTIVE", "INACTIVE")
    }
}

fun ChatInputCommandInteraction.getStatusParameter(): Status? {
    return command.strings[StatusParameter.NAME]?.let { Status.valueOf(it) }
}
