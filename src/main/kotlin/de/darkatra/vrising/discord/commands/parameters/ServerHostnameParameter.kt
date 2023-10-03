package de.darkatra.vrising.discord.commands.parameters

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string

object ServerHostnameParameter {
    const val NAME = "server-hostname"

    fun validate(serverHostname: String, allowLocalAddressRanges: Boolean) {
        HostnameValidator.validate(serverHostname, allowLocalAddressRanges, NAME)
    }
}

fun GlobalChatInputCreateBuilder.addServerHostnameParameter(required: Boolean = true) {
    string(
        name = ServerHostnameParameter.NAME,
        description = "The hostname of the server to add a status monitor for."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getServerHostnameParameter(): String? {
    return command.strings[ServerHostnameParameter.NAME]
}
