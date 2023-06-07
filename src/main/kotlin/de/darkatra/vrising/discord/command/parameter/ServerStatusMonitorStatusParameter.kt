package de.darkatra.vrising.discord.command.parameter

import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitorStatus
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string

private const val PARAMETER_NAME = "status"

fun GlobalChatInputCreateBuilder.addServerStatusMonitorStatusParameter(required: Boolean = true) {
    string(
        name = PARAMETER_NAME,
        description = "The status of the server status monitor. Either ACTIVE or INACTIVE."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getServerStatusMonitorStatusParameter(): ServerStatusMonitorStatus? {
    return command.strings[PARAMETER_NAME]?.let { ServerStatusMonitorStatus.valueOf(it) }
}
