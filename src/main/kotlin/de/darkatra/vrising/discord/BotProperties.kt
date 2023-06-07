package de.darkatra.vrising.discord

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.time.DurationMin
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.nio.file.Path
import java.time.Duration

@Validated
@ConfigurationProperties("bot")
class BotProperties {

    @field:NotBlank
    lateinit var discordBotToken: String

    @field:NotNull
    lateinit var databasePath: Path

    @field:NotBlank
    lateinit var databaseUsername: String

    @field:NotBlank
    lateinit var databasePassword: String

    @field:NotNull
    @field:DurationMin(seconds = 30)
    lateinit var updateDelay: Duration

    @field:Min(0)
    @field:NotNull
    var maxFailedAttempts: Int = 0

    @field:Min(0)
    @field:NotNull
    var maxRecentErrors: Int = 5

    @field:Min(1)
    @field:NotNull
    var maxCharactersPerError: Int = 200
}
