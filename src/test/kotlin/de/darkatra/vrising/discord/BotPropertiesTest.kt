package de.darkatra.vrising.discord

import jakarta.validation.Validation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Path
import java.time.Duration

class BotPropertiesTest {

    private val validator = Validation.buildDefaultValidatorFactory().use { it.validator }

    @Test
    fun `should be valid`() {

        val botProperties = getValidBotProperties()

        val result = validator.validate(botProperties)

        assertThat(result).hasSize(0)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  ", "\t"])
    fun `should be invalid if discordBotToken is blank`(discordBotToken: String) {

        val botProperties = getValidBotProperties().apply {
            this.discordBotToken = discordBotToken
        }

        val result = validator.validate(botProperties)

        assertThat(result).hasSize(1)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  ", "\t"])
    fun `should be invalid if databaseUsername is blank`(databaseUsername: String) {

        val botProperties = getValidBotProperties().apply {
            this.databaseUsername = databaseUsername
        }

        val result = validator.validate(botProperties)

        assertThat(result).hasSize(1)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  ", "\t"])
    fun `should be invalid if databasePassword is blank`(databasePassword: String) {

        val botProperties = getValidBotProperties().apply {
            this.databasePassword = databasePassword
        }

        val result = validator.validate(botProperties)

        assertThat(result).hasSize(1)
    }

    @Test
    fun `should be invalid if updateDelay is below 30 seconds`() {

        val botProperties = getValidBotProperties().apply {
            this.updateDelay = Duration.ofSeconds(29)
        }

        val result = validator.validate(botProperties)

        assertThat(result).hasSize(1)
    }

    @ParameterizedTest
    @ValueSource(ints = [-99, -1])
    fun `should be invalid if maxFailedAttempts is below 1`(maxFailedAttempts: Int) {

        val botProperties = getValidBotProperties().apply {
            this.maxFailedAttempts = maxFailedAttempts
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
            maxFailedAttempts = 5
        }
    }
}
