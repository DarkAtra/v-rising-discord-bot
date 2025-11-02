package de.darkatra.vrising.discord

import jakarta.validation.constraints.Max
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
    var databasePath: Path = Path.of("./bot.db")

    @field:NotBlank
    var databaseUsername: String = "v-rising-discord-bot"

    @field:NotBlank
    lateinit var databasePassword: String

    @field:NotNull
    @field:DurationMin(seconds = 5)
    var updateDelay: Duration = Duration.ofMinutes(1)

    @field:Min(1)
    @field:Max(100)
    @field:NotNull
    var updateThreadCount: Int = 10

    @field:Min(0)
    @field:NotNull
    var maxFailedAttempts: Int = 0

    @field:Min(0)
    @field:NotNull
    var maxFailedApiAttempts: Int = 0

    @field:Min(0)
    @field:NotNull
    var maxRecentErrors: Int = 5

    @field:Min(1)
    @field:NotNull
    var maxCharactersPerError: Int = 200

    @field:NotNull
    var allowLocalAddressRanges: Boolean = true

    @field:NotNull
    var adminUserIds: Set<@NotBlank String> = emptySet()

    @field:NotNull
    var cleanupJobEnabled: Boolean = false

    @field:NotNull
    var databaseBackupJobEnabled: Boolean = false

    @field:NotNull
    var databaseBackupDirectory: Path = Path.of("./database-backups/")

    @field:Min(1)
    @field:NotNull
    var databaseBackupMaxFiles: Int = 10

    @field:DurationMin(seconds = 1)
    var companionConnectTimeout: Duration = Duration.ofSeconds(2)

    @field:DurationMin(seconds = 1)
    var companionRequestTimeout: Duration = Duration.ofSeconds(10)

    @field:DurationMin(seconds = 1)
    var companionSocketTimeout: Duration = Duration.ofSeconds(5)
}
