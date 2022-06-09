package de.darkatra.vrising.discord.command

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.boolean
import dev.kord.rest.builder.interaction.int
import dev.kord.rest.builder.interaction.string

private const val SERVER_STATUS_MONITOR_ID_PARAMETER_NAME = "server-status-monitor-id"
private const val SERVER_HOSTNAME_PARAMETER_NAME = "server-hostname"
private const val SERVER_QUERY_PORT_PARAMETER_NAME = "server-query-port"
private const val DISPLAY_PLAYER_GEAR_LEVEL_PARAMETER_NAME = "display-player-gear-level"

fun GlobalChatInputCreateBuilder.addServerStatusMonitorIdParameter() {
    string(
        name = SERVER_STATUS_MONITOR_ID_PARAMETER_NAME,
        description = "The id of the server status monitor."
    ) {
        required = true
    }
}

fun ChatInputCommandInteraction.getServerStatusMonitorIdParameter(): String {
    return command.strings[SERVER_STATUS_MONITOR_ID_PARAMETER_NAME]!!
}

fun GlobalChatInputCreateBuilder.addServerHostnameParameter(required: Boolean = true) {
    string(
        name = SERVER_HOSTNAME_PARAMETER_NAME,
        description = "The hostname of the server to add a status monitor for."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getServerHostnameParameter(): String? {
    return command.strings[SERVER_HOSTNAME_PARAMETER_NAME]
}

fun GlobalChatInputCreateBuilder.addServerQueryPortParameter(required: Boolean = true) {
    int(
        name = SERVER_QUERY_PORT_PARAMETER_NAME,
        description = "The query port of the server to add a status monitor for."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getServerQueryPortParameter(): Int? {
    return command.integers[SERVER_QUERY_PORT_PARAMETER_NAME]?.let { Math.toIntExact(it) }
}

fun GlobalChatInputCreateBuilder.addDisplayPlayerGearLevelParameter(required: Boolean = true) {
    boolean(
        name = DISPLAY_PLAYER_GEAR_LEVEL_PARAMETER_NAME,
        description = "Whether or not to display the gear level in the player list."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getDisplayPlayerGearLevelParameter(): Boolean? {
    return command.booleans[DISPLAY_PLAYER_GEAR_LEVEL_PARAMETER_NAME]
}
