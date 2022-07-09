package de.darkatra.vrising.discord

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Path
import java.time.Duration
import javax.validation.Validation

internal class BotPropertiesTest {

    private val validator = Validation.buildDefaultValidatorFactory().use { it.validator }

    @Test
    internal fun `should be valid`() {

        val botProperties = getValidBotProperties()

        val result = validator.validate(botProperties)

        assertThat(result).hasSize(0)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  ", "\t"])
    internal fun `should be invalid if discordBotToken is blank`(discordBotToken: String) {

        val botProperties = getValidBotProperties().apply {
            this.discordBotToken = discordBotToken
        }

        val result = validator.validate(botProperties)

        assertThat(result).hasSize(1)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  ", "\t"])
    internal fun `should be invalid if databaseUsername is blank`(databaseUsername: String) {

        val botProperties = getValidBotProperties().apply {
            this.databaseUsername = databaseUsername
        }

        val result = validator.validate(botProperties)

        assertThat(result).hasSize(1)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  ", "\t"])
    internal fun `should be invalid if databasePassword is blank`(databasePassword: String) {

        val botProperties = getValidBotProperties().apply {
            this.databasePassword = databasePassword
        }

        val result = validator.validate(botProperties)

        assertThat(result).hasSize(1)
    }

    @Test
    internal fun `should be invalid if updateDelay is below 30 seconds`() {

        val botProperties = getValidBotProperties().apply {
            this.updateDelay = Duration.ofSeconds(29)
        }

        val result = validator.validate(botProperties)

        assertThat(result).hasSize(1)
    }

    private fun getValidBotProperties(): BotProperties {
        return BotProperties().apply {
            discordBotToken = "discord-token"
            databasePath = Path.of("test.db")
            databaseUsername = "username"
            databasePassword = "password"
            updateDelay = Duration.ofSeconds(30)
        }
    }
}
