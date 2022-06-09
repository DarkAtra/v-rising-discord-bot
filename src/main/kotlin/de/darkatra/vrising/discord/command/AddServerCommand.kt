package de.darkatra.vrising.discord.command

import com.fasterxml.uuid.Generators
import de.darkatra.vrising.discord.ServerStatusMonitor
import de.darkatra.vrising.discord.ServerStatusMonitorService
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import org.springframework.stereotype.Component

@Component
class AddServerCommand(
    private val serverStatusMonitorService: ServerStatusMonitorService,
) : Command {

    private val name: String = "add-server"
    private val description: String = "Adds a server to the status monitor."

    override fun getCommandName(): String = name

    override suspend fun register(kord: Kord) {

        kord.createGlobalChatInputCommand(
            name = name,
            description = description
        ) {

            addServerHostnameParameter()
            addServerQueryPortParameter()

            addDisplayPlayerGearLevelParameter(required = false)
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val hostName = interaction.getServerHostnameParameter()!!
        val queryPort = interaction.getServerQueryPortParameter()!!
        val displayPlayerGearLevel = interaction.getDisplayPlayerGearLevelParameter() ?: false

        val discordServerId = (interaction as GuildChatInputCommandInteraction).guildId
        val channelId = interaction.channelId

        serverStatusMonitorService.putServerStatusMonitor(
            ServerStatusMonitor(
                id = Generators.timeBasedGenerator().generate().toString(),
                discordServerId = discordServerId.toString(),
                discordChannelId = channelId.toString(),
                hostName = hostName,
                queryPort = queryPort,
                displayPlayerGearLevel = displayPlayerGearLevel
            )
        )

        interaction.deferEphemeralResponse().respond {
            content = "Added monitor for '${hostName}:${queryPort}' to channel '$channelId'. It might take up to 1 minute for the status post to appear."
        }
    }
}
