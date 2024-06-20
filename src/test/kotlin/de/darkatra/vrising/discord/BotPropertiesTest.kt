package de.darkatra.vrising.discord

import jakarta.validation.Validation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
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
    fun `should be invalid if updateDelay is below 5 seconds`() {

        val botProperties = getValidBotProperties().apply {
            this.updateDelay = Duration.ofSeconds(4)
        }

        val result = validator.validate(botProperties)

        assertThat(result).hasSize(1)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 5])
    fun `should be valid if maxFailedAttempts is positive or zero`(maxFailedAttempts: Int) {

        val botProperties = getValidBotProperties().apply {
            this.maxFailedAttempts = maxFailedAttempts
        }

        val result = validator.validate(botProperties)

        assertThat(result).isEmpty()
    }

    @ParameterizedTest
    @ValueSource(ints = [-99, -1])
    fun `should be invalid if maxFailedAttempts is negative`(maxFailedAttempts: Int) {

        val botProperties = getValidBotProperties().apply {
            this.maxFailedAttempts = maxFailedAttempts
        }

        val result = validator.validate(botProperties)

        assertThat(result).hasSize(1)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 5])
    fun `should be valid if maxFailedApiAttempts is positive or zero`(maxFailedApiAttempts: Int) {

        val botProperties = getValidBotProperties().apply {
            this.maxFailedApiAttempts = maxFailedApiAttempts
        }

        val result = validator.validate(botProperties)

        assertThat(result).isEmpty()
    }

    @ParameterizedTest
    @ValueSource(ints = [-99, -1])
    fun `should be invalid if maxFailedApiAttempts is negative`(maxFailedApiAttempts: Int) {

        val botProperties = getValidBotProperties().apply {
            this.maxFailedApiAttempts = maxFailedApiAttempts
        }

        val result = validator.validate(botProperties)

        assertThat(result).hasSize(1)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 5])
    fun `should be valid if maxRecentErrors is positive or zero`(maxRecentErrors: Int) {

        val botProperties = getValidBotProperties().apply {
            this.maxRecentErrors = maxRecentErrors
        }

        val result = validator.validate(botProperties)

        assertThat(result).isEmpty()
    }

    @ParameterizedTest
    @ValueSource(ints = [-99, -1])
    fun `should be invalid if maxRecentErrors is negative`(maxRecentErrors: Int) {

        val botProperties = getValidBotProperties().apply {
            this.maxRecentErrors = maxRecentErrors
        }

        val result = validator.validate(botProperties)

        assertThat(result).hasSize(1)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 200])
    fun `should be valid if maxCharactersPerError is positive`(maxCharactersPerError: Int) {

        val botProperties = getValidBotProperties().apply {
            this.maxCharactersPerError = maxCharactersPerError
        }

        val result = validator.validate(botProperties)

        assertThat(result).isEmpty()
    }

    @ParameterizedTest
    @ValueSource(ints = [-99, -1, 0])
    fun `should be invalid if maxCharactersPerError is negative or zero`(maxCharactersPerError: Int) {

        val botProperties = getValidBotProperties().apply {
            this.maxCharactersPerError = maxCharactersPerError
        }

        val result = validator.validate(botProperties)

        assertThat(result).hasSize(1)
    }

    @Test
    fun `should be valid with adminUserIds`() {

        val botProperties = getValidBotProperties().apply {
            this.adminUserIds = setOf("test-admin-id")
        }

        val result = validator.validate(botProperties)

        assertThat(result).isEmpty()
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  ", "\t"])
    fun `should be invalid if adminUserIds contains blank string`(adminUserId: String) {

        val botProperties = getValidBotProperties().apply {
            this.adminUserIds = setOf(adminUserId)
        }

        val result = validator.validate(botProperties)

        assertThat(result).hasSize(1)
    }

    private fun getValidBotProperties(): BotProperties {
        return BotProperties().apply {
            discordBotToken = "discord-token"
            databasePassword = "password"
        }
    }
}
