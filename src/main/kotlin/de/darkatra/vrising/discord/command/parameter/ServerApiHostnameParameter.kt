package de.darkatra.vrising.discord.command.parameter

import de.darkatra.vrising.discord.command.ValidationException
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import org.apache.commons.validator.routines.DomainValidator
import org.apache.commons.validator.routines.InetAddressValidator

private const val PARAMETER_NAME = "server-api-hostname"

fun GlobalChatInputCreateBuilder.addServerApiHostnameParameter(required: Boolean = true) {
    string(
        name = PARAMETER_NAME,
        description = "The hostname to use when querying the server's api."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getServerApiHostnameParameter(): String? {
    return command.strings[PARAMETER_NAME]
}

object ServerApiHostnameParameter {
    fun validate(serverApiHostname: String?) {
        if (serverApiHostname == null) {
            return
        }
        if (!InetAddressValidator.getInstance().isValid(serverApiHostname) && !DomainValidator.getInstance(true).isValid(serverApiHostname)) {
            throw ValidationException("'$PARAMETER_NAME' is not a valid ip address or domain name. Rejected: $serverApiHostname")
        }
    }
}
