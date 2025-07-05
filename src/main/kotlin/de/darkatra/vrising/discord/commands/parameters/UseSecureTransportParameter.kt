package de.darkatra.vrising.discord.commands.parameters

import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string

object UseSecureTransportParameter {
    const val NAME = "use-secure-transport"
}

fun GlobalChatInputCreateBuilder.addUseSecureTransportParameter(required: Boolean = true) {
    string(
        name = UseSecureTransportParameter.NAME,
        description = "Whether api requests should use https or not. Defaults to false."
    ) {
        this.required = required
    }
}

fun ChatInputCommandInteraction.getUseSecureTransportParameter(): Boolean? {
    return command.booleans[UseSecureTransportParameter.NAME]
}
