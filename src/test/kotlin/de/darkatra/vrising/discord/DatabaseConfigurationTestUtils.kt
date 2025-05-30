package de.darkatra.vrising.discord

import de.darkatra.vrising.discord.persistence.DatabaseConfiguration
import org.dizitart.no2.Nitrite
import org.dizitart.no2.filters.Filter
import org.slf4j.LoggerFactory
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream

object DatabaseConfigurationTestUtils {

    val DATABASE_FILE_V1_2_x by lazy { DatabaseConfigurationTestUtils::class.java.getResource("/persistence/v1.2.db")!! }
    val DATABASE_FILE_V2_10_5 by lazy { DatabaseConfigurationTestUtils::class.java.getResource("/persistence/v2.10.5.db")!! }
    val DATABASE_FILE_V2_10_5_WITH_PASSWORD by lazy { DatabaseConfigurationTestUtils::class.java.getResource("/persistence/v2.10.5-with-password.db")!! }
    val DATABASE_FILE_V2_12_0_WITH_PASSWORD by lazy { DatabaseConfigurationTestUtils::class.java.getResource("/persistence/v2.12.0-with-password.db")!! }

    private val logger by lazy { LoggerFactory.getLogger(javaClass) }

    fun getTestDatabase(fromTemplate: URL? = null, username: String? = null, password: String? = null): Nitrite {

        val databaseFile = Files.createTempFile("v-rising-bot", ".db").also {
            logger.info("Test Db location: " + it.absolutePathString())
        }.also { it.deleteIfExists() }

        if (fromTemplate != null) {
            logger.info("Loading template from '$fromTemplate'.")
            fromTemplate.openStream().buffered().use { inputStream ->
                databaseFile.outputStream(CREATE, TRUNCATE_EXISTING).buffered().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }

        return DatabaseConfiguration.buildNitriteDatabase(databaseFile, username, password)
    }

    fun clearDatabase(nitrite: Nitrite) {
        nitrite.listCollectionNames().forEach { collectionName ->
            nitrite.getCollection(collectionName).use {
                it.remove(Filter.ALL)
            }
        }
    }
}
