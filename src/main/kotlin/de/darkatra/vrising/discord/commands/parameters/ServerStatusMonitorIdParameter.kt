package de.darkatra.vrising.discord.commands.parameters

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string

object ServerStatusMonitorIdParameter {
    const val NAME = "server-status-monitor-id"
}

fun GlobalChatInputCreateBuilder.addServerStatusMonitorIdParameter() {
    string(
        name = ServerStatusMonitorIdParameter.NAME,
        description = "The id of the server status monitor."
    ) {
        required = true
    }
}

fun ChatInputCommandInteraction.getServerStatusMonitorIdParameter(): String {
    return command.strings[ServerStatusMonitorIdParameter.NAME]!!
}
