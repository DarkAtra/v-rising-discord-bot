package de.darkatra.vrising.discord.persistence

import de.darkatra.vrising.discord.BotProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.io.path.absolutePathString
import kotlin.io.path.copyTo
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.notExists
import kotlin.streams.asSequence

@Service
@EnableConfigurationProperties(BotProperties::class)
class DatabaseBackupService(
    private val botProperties: BotProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ssX").withZone(ZoneOffset.UTC)

    fun performDatabaseBackup() {

        val databaseFile = botProperties.databasePath
        if (databaseFile.notExists()) {
            logger.warn("Aborting backup process because the database does not exist: ${databaseFile.absolutePathString()}")
            return
        }

        val databaseBackupDirectory = botProperties.databaseBackupDirectory
        if (databaseBackupDirectory.notExists()) {
            databaseBackupDirectory.toFile().mkdirs()
        }

        if (!databaseBackupDirectory.isDirectory()) {
            logger.warn("Aborting backup process because the backup directory is not a directory: ${databaseBackupDirectory.absolutePathString()}")
            return
        }

        deleteOldBackups()

        val backupFile = databaseBackupDirectory.resolve(dateTimeFormatter.format(Instant.now()))
        if (backupFile.exists()) {
            logger.warn("A backup file with the same name already exists... Overwriting the existing file: ${backupFile.absolutePathString()}")
        }

        botProperties.databasePath.copyTo(backupFile, overwrite = true)

        logger.info("Successfully created the database backup: ${backupFile.absolutePathString()}")
    }

    fun deleteOldBackups() {

        Files.list(botProperties.databaseBackupDirectory).use { fileStream ->
            fileStream.asSequence()
                .filter { file -> file.isRegularFile() }
                .mapNotNull { file ->
                    try {
                        Pair(file, Instant.from(dateTimeFormatter.parse(file.name)))
                    } catch (e: Exception) {
                        null
                    }
                }
                .sortedWith(Comparator.comparing<Pair<Path, Instant>, Instant> { it.second }.reversed())
                .drop(botProperties.databaseBackupMaxFiles - 1)
                .forEach { (fileToDelete, _) ->
                    logger.info("Deleting '${fileToDelete.absolutePathString()}'...")
                    fileToDelete.deleteExisting()
                }
        }
    }
}
