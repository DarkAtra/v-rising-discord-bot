package de.darkatra.vrising.discord.commands

import dev.kord.core.Kord
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GlobalChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction

interface Command {

    fun getCommandName(): String

    suspend fun register(kord: Kord)

    fun isSupported(interaction: ChatInputCommandInteraction, adminUserIds: Set<String>): Boolean {

        if (interaction.invokedCommandName != getCommandName() || interaction.user.isBot) {
            return false
        }

        return when (interaction) {
            is GuildChatInputCommandInteraction -> true
            is GlobalChatInputCommandInteraction -> adminUserIds.contains(interaction.user.id.toString())
        }
    }

    suspend fun handle(interaction: ChatInputCommandInteraction)
}
