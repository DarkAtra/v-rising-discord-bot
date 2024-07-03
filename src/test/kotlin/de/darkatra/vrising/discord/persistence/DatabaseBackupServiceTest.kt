package de.darkatra.vrising.discord.persistence

import de.darkatra.vrising.discord.BotProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import java.nio.file.Files
import java.nio.file.Path
import java.util.regex.Pattern
import kotlin.io.path.absolutePathString
import kotlin.io.path.createFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

@ExtendWith(OutputCaptureExtension::class)
class DatabaseBackupServiceTest {

    @Test
    fun `should create database backups`() {

        val databasePath = Files.createTempFile("v-rising-bot", "db")
        val databaseBackupDirectory = Files.createTempDirectory("v-rising-bot-backup-dir")

        val databaseBackupService = DatabaseBackupService(getBotProperties(databasePath, databaseBackupDirectory))

        databaseBackupService.performDatabaseBackup()

        val files = databaseBackupDirectory.listDirectoryEntries()

        assertThat(files).hasSize(1)
        assertThat(files[0].name).matches(Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2}Z"))
        assertThat(files[0]).hasSameBinaryContentAs(databasePath)
    }

    @Test
    fun `should not create database backups when databaseBackupDirectory is a file`(capturedOutput: CapturedOutput) {

        val databasePath = Files.createTempFile("v-rising-bot", "db")
        val databaseBackupDirectory = Files.createTempFile("v-rising-bot-backup-dir", "as-file")

        val databaseBackupService = DatabaseBackupService(getBotProperties(databasePath, databaseBackupDirectory))

        databaseBackupService.performDatabaseBackup()

        assertThat(capturedOutput.out).contains("Aborting backup process because the backup directory is not a directory:")
    }

    @Test
    fun `should delete old database backups`() {

        val databasePath = Files.createTempFile("v-rising-bot", "db")
        val databaseBackupDirectory = Files.createTempDirectory("v-rising-bot-backup-dir")

        databaseBackupDirectory.resolve("2024-01-01_00-00-00Z").createFile()
        databaseBackupDirectory.resolve("2024-01-01_01-00-00Z").createFile()

        val fileToKeep = databaseBackupDirectory.resolve("2024-01-01_02-00-00Z").createFile()

        val databaseBackupService = DatabaseBackupService(getBotProperties(databasePath, databaseBackupDirectory))

        databaseBackupService.deleteOldBackups()

        val files = databaseBackupDirectory.listDirectoryEntries()

        assertThat(files).hasSize(1)
        assertThat(files[0].absolutePathString()).isEqualTo(fileToKeep.absolutePathString())
    }

    @Test
    fun `should not delete old database backups if below max files`() {

        val databasePath = Files.createTempFile("v-rising-bot", "db")
        val databaseBackupDirectory = Files.createTempDirectory("v-rising-bot-backup-dir")

        val fileToKeep = databaseBackupDirectory.resolve("2024-01-01_02-00-00Z").createFile()

        val databaseBackupService = DatabaseBackupService(getBotProperties(databasePath, databaseBackupDirectory))

        databaseBackupService.deleteOldBackups()

        val files = databaseBackupDirectory.listDirectoryEntries()

        assertThat(files).hasSize(1)
        assertThat(files[0].absolutePathString()).isEqualTo(fileToKeep.absolutePathString())
    }

    private fun getBotProperties(databasePath: Path, databaseBackupDirectory: Path): BotProperties {
        return BotProperties().apply {
            discordBotToken = "discord-token"
            databasePassword = "password"
            this.databasePath = databasePath
            databaseBackupJobEnabled = true
            databaseBackupMaxFiles = 2
            this.databaseBackupDirectory = databaseBackupDirectory
        }
    }
}
