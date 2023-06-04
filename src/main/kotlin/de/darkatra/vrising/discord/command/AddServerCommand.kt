package de.darkatra.vrising.discord.command

import com.fasterxml.uuid.Generators
import de.darkatra.vrising.discord.command.parameter.ServerHostnameParameter
import de.darkatra.vrising.discord.command.parameter.addDisplayClanParameter
import de.darkatra.vrising.discord.command.parameter.addDisplayGearLevelParameter
import de.darkatra.vrising.discord.command.parameter.addDisplayKilledVBloodsParameter
import de.darkatra.vrising.discord.command.parameter.addDisplayServerDescriptionParameter
import de.darkatra.vrising.discord.command.parameter.addServerApiPortParameter
import de.darkatra.vrising.discord.command.parameter.addServerHostnameParameter
import de.darkatra.vrising.discord.command.parameter.addServerQueryPortParameter
import de.darkatra.vrising.discord.command.parameter.getDisplayClanParameter
import de.darkatra.vrising.discord.command.parameter.getDisplayGearLevelParameter
import de.darkatra.vrising.discord.command.parameter.getDisplayKilledVBloodsParameter
import de.darkatra.vrising.discord.command.parameter.getDisplayServerDescriptionParameter
import de.darkatra.vrising.discord.command.parameter.getServerApiPortParameter
import de.darkatra.vrising.discord.command.parameter.getServerHostnameParameter
import de.darkatra.vrising.discord.command.parameter.getServerQueryPortParameter
import de.darkatra.vrising.discord.serverstatus.ServerStatusMonitorService
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitorStatus
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

            dmPermission = false
            disableCommandInGuilds()

            addServerHostnameParameter()
            addServerQueryPortParameter()
            addServerApiPortParameter(required = false)

            addDisplayServerDescriptionParameter(required = false)
            addDisplayClanParameter(required = false)
            addDisplayGearLevelParameter(required = false)
            addDisplayKilledVBloodsParameter(required = false)
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val hostName = interaction.getServerHostnameParameter()!!
        val queryPort = interaction.getServerQueryPortParameter()!!
        val apiPort = interaction.getServerApiPortParameter()

        val displayServerDescription = interaction.getDisplayServerDescriptionParameter() ?: true
        val displayClan = interaction.getDisplayClanParameter() ?: true
        val displayGearLevel = interaction.getDisplayGearLevelParameter() ?: true
        val displayKilledVBloods = interaction.getDisplayKilledVBloodsParameter() ?: true

        val discordServerId = (interaction as GuildChatInputCommandInteraction).guildId
        val channelId = interaction.channelId

        ServerHostnameParameter.validate(hostName)

        val serverStatusMonitorId = Generators.timeBasedGenerator().generate()
        serverStatusMonitorService.putServerStatusMonitor(
            ServerStatusMonitor(
                id = serverStatusMonitorId.toString(),
                discordServerId = discordServerId.toString(),
                discordChannelId = channelId.toString(),
                hostName = hostName,
                queryPort = queryPort,
                apiPort = apiPort,
                status = ServerStatusMonitorStatus.ACTIVE,
                displayServerDescription = displayServerDescription,
                displayClan = displayClan,
                displayGearLevel = displayGearLevel,
                displayKilledVBloods = displayKilledVBloods
            )
        )

        interaction.deferEphemeralResponse().respond {
            content = """Added monitor with id '${serverStatusMonitorId}' for '${hostName}:${queryPort}' to channel '$channelId'.
                |It may take some time until the status message appears.""".trimMargin()
        }
    }
}
