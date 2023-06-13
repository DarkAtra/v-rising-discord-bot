package de.darkatra.vrising.discord.commands.parameters

import de.darkatra.vrising.discord.commands.exceptions.ValidationException
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import org.apache.commons.validator.routines.DomainValidator
import org.apache.commons.validator.routines.InetAddressValidator

object ServerHostnameParameter {
    const val NAME = "server-hostname"

    fun validate(serverHostname: String) {
        if (!InetAddressValidator.getInstance().isValid(serverHostname) && !DomainValidator.getInstance(true).isValid(serverHostname)) {
            throw ValidationException("'$NAME' is not a valid ip address or domain name. Rejected: $serverHostname")
        }
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
