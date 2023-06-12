package de.darkatra.vrising.discord.command

import com.fasterxml.uuid.Generators
import de.darkatra.vrising.discord.command.parameter.ServerApiHostnameParameter
import de.darkatra.vrising.discord.command.parameter.ServerHostnameParameter
import de.darkatra.vrising.discord.command.parameter.addDisplayPlayerGearLevelParameter
import de.darkatra.vrising.discord.command.parameter.addDisplayServerDescriptionParameter
import de.darkatra.vrising.discord.command.parameter.addPlayerActivityFeedChannelIdParameter
import de.darkatra.vrising.discord.command.parameter.addServerApiHostnameParameter
import de.darkatra.vrising.discord.command.parameter.addServerApiPortParameter
import de.darkatra.vrising.discord.command.parameter.addServerHostnameParameter
import de.darkatra.vrising.discord.command.parameter.addServerQueryPortParameter
import de.darkatra.vrising.discord.command.parameter.getDisplayPlayerGearLevelParameter
import de.darkatra.vrising.discord.command.parameter.getDisplayServerDescriptionParameter
import de.darkatra.vrising.discord.command.parameter.getPlayerActivityFeedChannelIdParameter
import de.darkatra.vrising.discord.command.parameter.getServerApiHostnameParameter
import de.darkatra.vrising.discord.command.parameter.getServerApiPortParameter
import de.darkatra.vrising.discord.command.parameter.getServerHostnameParameter
import de.darkatra.vrising.discord.command.parameter.getServerQueryPortParameter
import de.darkatra.vrising.discord.serverstatus.ServerStatusMonitorRepository
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitorStatus
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import org.springframework.stereotype.Component

@Component
class AddServerCommand(
    private val serverStatusMonitorRepository: ServerStatusMonitorRepository,
) : Command {

    private val name: String = "add-server"
    private val description: String = "Adds a server to the status monitor."

    override fun getCommandName(): String = name

    override suspend fun register(kord: Kord) {

        kord.createGlobalChatInputCommand(
            name = name,
            description = description
        ) {

            dmPermission = false
            disableCommandInGuilds()

            addServerHostnameParameter()
            addServerQueryPortParameter()
            addServerApiHostnameParameter(required = false)
            addServerApiPortParameter(required = false)

            addDisplayServerDescriptionParameter(required = false)
            addDisplayPlayerGearLevelParameter(required = false)

            addPlayerActivityFeedChannelIdParameter(required = false)
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val hostname = interaction.getServerHostnameParameter()!!
        val queryPort = interaction.getServerQueryPortParameter()!!
        val apiHostname = interaction.getServerApiHostnameParameter()
        val apiPort = interaction.getServerApiPortParameter()

        val displayServerDescription = interaction.getDisplayServerDescriptionParameter() ?: true
        val displayPlayerGearLevel = interaction.getDisplayPlayerGearLevelParameter() ?: true

        val playerActivityFeedChannelId = interaction.getPlayerActivityFeedChannelIdParameter()

        val discordServerId = (interaction as GuildChatInputCommandInteraction).guildId
        val channelId = interaction.channelId

        ServerHostnameParameter.validate(hostname)
        ServerApiHostnameParameter.validate(apiHostname)

        val serverStatusMonitorId = Generators.timeBasedGenerator().generate()
        serverStatusMonitorRepository.addServerStatusMonitor(
            ServerStatusMonitor(
                id = serverStatusMonitorId.toString(),
                discordServerId = discordServerId.toString(),
                discordChannelId = channelId.toString(),
                playerActivityDiscordChannelId = playerActivityFeedChannelId,
                hostname = hostname,
                queryPort = queryPort,
                apiHostname = apiHostname,
                apiPort = apiPort,
                status = ServerStatusMonitorStatus.ACTIVE,
                displayServerDescription = displayServerDescription,
                displayPlayerGearLevel = displayPlayerGearLevel,
            )
        )

        interaction.deferEphemeralResponse().respond {
            content = """Added monitor with id '${serverStatusMonitorId}' for '${hostname}:${queryPort}' to channel '$channelId'.
                |It may take some time until the status message appears.""".trimMargin()
        }
    }
}
