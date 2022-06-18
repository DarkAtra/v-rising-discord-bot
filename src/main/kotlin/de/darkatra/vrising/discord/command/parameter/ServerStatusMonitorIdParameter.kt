package de.darkatra.vrising.discord.command.parameter

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string

private const val PARAMETER_NAME = "server-status-monitor-id"

fun GlobalChatInputCreateBuilder.addServerStatusMonitorIdParameter() {
    string(
        name = PARAMETER_NAME,
        description = "The id of the server status monitor."
    ) {
        required = true
    }
}

fun ChatInputCommandInteraction.getServerStatusMonitorIdParameter(): String {
    return command.strings[PARAMETER_NAME]!!
}
