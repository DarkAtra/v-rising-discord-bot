package de.darkatra.vrising.discord.commands

import com.fasterxml.uuid.Generators
import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.commands.parameters.ServerApiHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.ServerHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.addDisplayPlayerGearLevelParameter
import de.darkatra.vrising.discord.commands.parameters.addDisplayServerDescriptionParameter
import de.darkatra.vrising.discord.commands.parameters.addPlayerActivityFeedChannelIdParameter
import de.darkatra.vrising.discord.commands.parameters.addPvpKillFeedChannelIdParameter
import de.darkatra.vrising.discord.commands.parameters.addServerApiHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.addServerApiPortParameter
import de.darkatra.vrising.discord.commands.parameters.addServerHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.addServerQueryPortParameter
import de.darkatra.vrising.discord.commands.parameters.getDisplayPlayerGearLevelParameter
import de.darkatra.vrising.discord.commands.parameters.getDisplayServerDescriptionParameter
import de.darkatra.vrising.discord.commands.parameters.getPlayerActivityFeedChannelIdParameter
import de.darkatra.vrising.discord.commands.parameters.getPvpKillFeedChannelIdParameter
import de.darkatra.vrising.discord.commands.parameters.getServerApiHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.getServerApiPortParameter
import de.darkatra.vrising.discord.commands.parameters.getServerHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.getServerQueryPortParameter
import de.darkatra.vrising.discord.serverstatus.ServerStatusMonitorRepository
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitorStatus
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

@Component
@EnableConfigurationProperties(BotProperties::class)
class AddServerCommand(
    private val serverStatusMonitorRepository: ServerStatusMonitorRepository,
    private val botProperties: BotProperties
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
            addPvpKillFeedChannelIdParameter(required = false)
        }
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val hostname = interaction.getServerHostnameParameter()!!
        val queryPort = interaction.getServerQueryPortParameter()!!
        val apiHostname = interaction.getServerApiHostnameParameter()
        val apiPort = interaction.getServerApiPortParameter()

        val displayServerDescription = interaction.getDisplayServerDescriptionParameter() ?: true
        val displayPlayerGearLevel = interaction.getDisplayPlayerGearLevelParameter() ?: true

        val playerActivityChannelId = interaction.getPlayerActivityFeedChannelIdParameter()
        val pvpKillFeedChannelId = interaction.getPvpKillFeedChannelIdParameter()

        val discordServerId = (interaction as GuildChatInputCommandInteraction).guildId
        val channelId = interaction.channelId

        ServerHostnameParameter.validate(hostname, botProperties.allowLocalAddressRanges)
        ServerApiHostnameParameter.validate(apiHostname, botProperties.allowLocalAddressRanges)

        val serverStatusMonitorId = Generators.timeBasedGenerator().generate()
        serverStatusMonitorRepository.addServerStatusMonitor(
            ServerStatusMonitor(
                id = serverStatusMonitorId.toString(),
                discordServerId = discordServerId.toString(),
                discordChannelId = channelId.toString(),
                playerActivityDiscordChannelId = playerActivityChannelId,
                pvpKillFeedDiscordChannelId = pvpKillFeedChannelId,
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
