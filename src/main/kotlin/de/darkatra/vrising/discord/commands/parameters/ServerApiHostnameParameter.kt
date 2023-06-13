package de.darkatra.vrising.discord.commands.parameters

import de.darkatra.vrising.discord.commands.exceptions.ValidationException
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import org.apache.commons.validator.routines.DomainValidator
import org.apache.commons.validator.routines.InetAddressValidator

object ServerApiHostnameParameter {
    const val NAME = "server-api-hostname"

    fun validate(serverApiHostname: String?) {
        if (serverApiHostname == null) {
            return
        }
        if (!InetAddressValidator.getInstance().isValid(serverApiHostname) && !DomainValidator.getInstance(true).isValid(serverApiHostname)) {
            throw ValidationException("'$NAME' is not a valid ip address or domain name. Rejected: $serverApiHostname")
        }
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
