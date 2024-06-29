package de.darkatra.vrising.discord.commands

import com.fasterxml.uuid.Generators
import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.commands.parameters.ServerApiHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.ServerHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.addServerApiHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.addServerApiPasswordParameter
import de.darkatra.vrising.discord.commands.parameters.addServerApiPortParameter
import de.darkatra.vrising.discord.commands.parameters.addServerApiUsernameParameter
import de.darkatra.vrising.discord.commands.parameters.addServerHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.addServerQueryPortParameter
import de.darkatra.vrising.discord.commands.parameters.getServerApiHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.getServerApiPasswordParameter
import de.darkatra.vrising.discord.commands.parameters.getServerApiPortParameter
import de.darkatra.vrising.discord.commands.parameters.getServerApiUsernameParameter
import de.darkatra.vrising.discord.commands.parameters.getServerHostnameParameter
import de.darkatra.vrising.discord.commands.parameters.getServerQueryPortParameter
import de.darkatra.vrising.discord.persistence.ServerRepository
import de.darkatra.vrising.discord.persistence.model.Server
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.entity.interaction.GlobalChatInputCommandInteraction
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

@Component
@EnableConfigurationProperties(BotProperties::class)
class AddServerCommand(
    private val serverRepository: ServerRepository,
    private val botProperties: BotProperties
) : Command {

    private val logger by lazy { LoggerFactory.getLogger(javaClass) }

    private val name: String = "add-server"
    private val description: String = "Adds a server."

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
            addServerApiUsernameParameter(required = false)
            addServerApiPasswordParameter(required = false)
        }
    }

    override fun isSupported(interaction: ChatInputCommandInteraction, adminUserIds: Set<String>): Boolean {
        if (interaction is GlobalChatInputCommandInteraction) {
            return false
        }
        return super.isSupported(interaction, adminUserIds)
    }

    override suspend fun handle(interaction: ChatInputCommandInteraction) {

        val hostname = interaction.getServerHostnameParameter()!!
        val queryPort = interaction.getServerQueryPortParameter()!!

        val apiHostname = interaction.getServerApiHostnameParameter()
        val apiPort = interaction.getServerApiPortParameter()
        val apiUsername = interaction.getServerApiUsernameParameter()
        val apiPassword = interaction.getServerApiPasswordParameter()

        val discordServerId = (interaction as GuildChatInputCommandInteraction).guildId

        ServerHostnameParameter.validate(hostname, botProperties.allowLocalAddressRanges)
        ServerApiHostnameParameter.validate(apiHostname, botProperties.allowLocalAddressRanges)

        val serverId = Generators.timeBasedGenerator().generate()
        serverRepository.addServer(
            Server(
                id = serverId.toString(),
                discordServerId = discordServerId.toString(),
                hostname = hostname,
                queryPort = queryPort,
                apiHostname = apiHostname,
                apiPort = apiPort,
                apiUsername = apiUsername,
                apiPassword = apiPassword,
            )
        )

        logger.info("Successfully added server '$serverId' for '$hostname:$queryPort' for discord server '$discordServerId'.")

        interaction.deferEphemeralResponse().respond {
            content = "Added server with id '$serverId' for '$hostname:$queryPort' for discord server '$discordServerId'."
        }
    }
}
