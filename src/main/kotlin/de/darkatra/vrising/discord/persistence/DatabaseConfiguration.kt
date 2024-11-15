package de.darkatra.vrising.discord.persistence

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.migration.SchemaEntityConverter
import de.darkatra.vrising.discord.migration.getNitriteMap
import de.darkatra.vrising.discord.migration.listAllCollectionNames
import de.darkatra.vrising.discord.persistence.model.converter.ErrorEntityConverter
import de.darkatra.vrising.discord.persistence.model.converter.PlayerActivityFeedEntityConverter
import de.darkatra.vrising.discord.persistence.model.converter.PvpKillFeedEntityConverter
import de.darkatra.vrising.discord.persistence.model.converter.ServerEntityConverter
import de.darkatra.vrising.discord.persistence.model.converter.StatusMonitorEntityConverter
import org.dizitart.no2.Nitrite
import org.dizitart.no2.NitriteBuilder
import org.dizitart.no2.exceptions.NitriteIOException
import org.dizitart.no2.mvstore.MVStoreModule
import org.dizitart.no2.store.StoreModule
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.copyTo
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.inputStream

@Configuration
@EnableConfigurationProperties(BotProperties::class)
class DatabaseConfiguration(
    private val botProperties: BotProperties
) {

    companion object {

        private val encryptedMarker = "H2encrypt"
        private val logger by lazy { LoggerFactory.getLogger(DatabaseConfiguration::class.java) }

        fun buildNitriteDatabase(databaseFile: Path, username: String? = null, password: String? = null): Nitrite {

            // version 2.12.0 introduced database encryption at rest. the following code attempts to perform the migration if necessary
            val firstFewBytes = databaseFile.inputStream().readNBytes(encryptedMarker.length).toString(StandardCharsets.UTF_8)
            if (firstFewBytes != encryptedMarker) {

                // if the automated migration was aborted while writing the files to disc, restore the backup
                val unencryptedDatabaseBackupFile = Path.of(System.getProperty("java.io.tmpdir")).resolve("v-rising-bot.db.unencrypted")
                if (unencryptedDatabaseBackupFile.exists()) {
                    logger.info("Found an unencrypted backup of the database at: ${unencryptedDatabaseBackupFile.absolutePathString()}")
                    unencryptedDatabaseBackupFile.copyTo(databaseFile, overwrite = true)
                    logger.info("Successfully restored the backup. Will re-attempt the migration.")
                }

                logger.info("Attempting to encrypt the bot database with the provided database password.")

                // retry opening the database without encryption if we encounter an error
                val unencryptedDatabase = try {
                    getNitriteBuilder(getStoreModule(databaseFile, null)).openOrCreate(username, password)
                } catch (e: NitriteIOException) {
                    throw IllegalStateException("Could not encrypt the database.", e)
                }

                unencryptedDatabaseBackupFile.deleteIfExists()

                // create an encrypted copy of the existing database
                val tempDatabaseFile = Files.createTempFile("v-rising-bot", ".db")

                val encryptedDatabase = getNitriteBuilder(getStoreModule(tempDatabaseFile, password)).openOrCreate(username, password)
                for (collectionName in unencryptedDatabase.listAllCollectionNames()) {

                    val oldCollection = unencryptedDatabase.getNitriteMap(collectionName)
                    val newCollection = encryptedDatabase.getNitriteMap(collectionName)

                    oldCollection.values().forEach { document -> newCollection.put(document.id, document) }
                }
                unencryptedDatabase.close()
                encryptedDatabase.close()

                databaseFile.copyTo(unencryptedDatabaseBackupFile)
                tempDatabaseFile.copyTo(databaseFile, overwrite = true)

                unencryptedDatabaseBackupFile.deleteIfExists()
                tempDatabaseFile.deleteIfExists()

                logger.info("Successfully encrypted the database.")
            }

            return getNitriteBuilder(getStoreModule(databaseFile, password)).openOrCreate(username, password)
        }

        private fun getNitriteBuilder(storeModule: StoreModule): NitriteBuilder {

            return Nitrite.builder()
                .loadModule(storeModule)
                .disableRepositoryTypeValidation()
                .registerEntityConverter(SchemaEntityConverter())
                .registerEntityConverter(ErrorEntityConverter())
                .registerEntityConverter(PlayerActivityFeedEntityConverter())
                .registerEntityConverter(PvpKillFeedEntityConverter())
                .registerEntityConverter(ServerEntityConverter())
                .registerEntityConverter(StatusMonitorEntityConverter())
        }

        private fun getStoreModule(databaseFile: Path, password: String?): MVStoreModule {

            return MVStoreModule.withConfig()
                .filePath(databaseFile.toAbsolutePath().toFile())
                .encryptionKey(password?.let(String::toCharArray))
                .compress(true)
                .build()
        }
    }

    @Bean
    fun database(): Nitrite {

        return buildNitriteDatabase(
            botProperties.databasePath,
            botProperties.databaseUsername,
            botProperties.databasePassword
        )
    }
}
