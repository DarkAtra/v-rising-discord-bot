package de.darkatra.vrising.discord.command.parameter

import de.darkatra.vrising.discord.command.ValidationException
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import org.apache.commons.validator.routines.DomainValidator
import org.apache.commons.validator.routines.InetAddressValidator

private const val PARAMETER_NAME = "server-hostname"

fun GlobalChatInputCreateBuilder.addServerHostnameParameter(required: Boolean = true) {
    string(
        name = PARAMETER_NAME,
        description = "The hostname of the server to add a status monitor for."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getServerHostnameParameter(): String? {
    return command.strings[PARAMETER_NAME]
}

object ServerHostnameParameter {
    fun validate(serverHostname: String) {
        if (!InetAddressValidator.getInstance().isValid(serverHostname) && !DomainValidator.getInstance(true).isValid(serverHostname)) {
            throw ValidationException("'$PARAMETER_NAME' is not a valid ip address or domain name. Rejected: $serverHostname")
        }
    }
}
