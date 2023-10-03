package de.darkatra.vrising.discord.commands.parameters

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string

object ServerApiHostnameParameter {
    const val NAME = "server-api-hostname"

    fun validate(serverApiHostname: String?, allowLocalAddressRanges: Boolean) {
        if (serverApiHostname == null) {
            return
        }
        HostnameValidator.validate(serverApiHostname, allowLocalAddressRanges, NAME)
    }
}

fun GlobalChatInputCreateBuilder.addServerApiHostnameParameter(required: Boolean = true) {
    string(
        name = ServerApiHostnameParameter.NAME,
        description = "The hostname to use when querying the server's api."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getServerApiHostnameParameter(): String? {
    return command.strings[ServerApiHostnameParameter.NAME]
}
