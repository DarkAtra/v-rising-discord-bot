package de.darkatra.vrising.discord.commands

import de.darkatra.vrising.discord.commands.parameters.getServerIdParameter
import de.darkatra.vrising.discord.persistence.ServerRepository
import de.darkatra.vrising.discord.persistence.model.Server
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GlobalChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction

suspend fun ChatInputCommandInteraction.getServer(serverRepository: ServerRepository): Server? {

    val serverId = getServerIdParameter()

    val server = when (this) {
        is GuildChatInputCommandInteraction -> serverRepository.getServer(serverId, guildId.toString())
        is GlobalChatInputCommandInteraction -> serverRepository.getServer(serverId)
    }

    if (server == null) {
        deferEphemeralResponse().respond {
            content = "No server with id '$serverId' was found."
        }
        return null
    }

    return server
}
