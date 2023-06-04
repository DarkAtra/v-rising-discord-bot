package de.darkatra.vrising.discord.command.parameter

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.boolean

private const val PARAMETER_NAME = "display-players-as-ascii-table"

fun GlobalChatInputCreateBuilder.addDisplayPlayersAsAsciiTable(required: Boolean = true) {
    boolean(
        name = PARAMETER_NAME,
        description = "Whether or not to display the player list as ASCII table. Defaults to false."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getDisplayPlayersAsAsciiTable(): Boolean? {
    return command.booleans[PARAMETER_NAME]
}
