package de.darkatra.vrising.discord.commands.parameters

import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitorStatus
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string

object ServerStatusMonitorStatusParameter {
    const val NAME = "status"
}

fun GlobalChatInputCreateBuilder.addServerStatusMonitorStatusParameter(required: Boolean = true) {
    string(
        name = ServerStatusMonitorStatusParameter.NAME,
        description = "Determines if a server status monitor should be updated or not."
    ) {
        this.required = required
        this.autocomplete = true

        choice("ACTIVE", "ACTIVE")
        choice("INACTIVE", "INACTIVE")
    }
}

fun ChatInputCommandInteraction.getServerStatusMonitorStatusParameter(): ServerStatusMonitorStatus? {
    return command.strings[ServerStatusMonitorStatusParameter.NAME]?.let { ServerStatusMonitorStatus.valueOf(it) }
}
