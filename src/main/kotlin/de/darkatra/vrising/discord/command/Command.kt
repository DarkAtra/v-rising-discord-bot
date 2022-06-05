package de.darkatra.vrising.discord.command

import dev.kord.core.Kord
import dev.kord.core.entity.interaction.ChatInputCommandInteraction

interface Command {

    fun getCommandName(): String

    suspend fun register(kord: Kord)

    fun isSupported(interaction: ChatInputCommandInteraction): Boolean {
        return interaction.invokedCommandName == getCommandName() && !interaction.user.isBot
    }

    suspend fun handle(interaction: ChatInputCommandInteraction)
}
