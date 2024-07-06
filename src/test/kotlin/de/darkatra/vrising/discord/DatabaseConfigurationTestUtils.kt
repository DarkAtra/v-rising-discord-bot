package de.darkatra.vrising.discord

import de.darkatra.vrising.discord.persistence.DatabaseConfiguration
import org.dizitart.no2.Nitrite
import org.dizitart.no2.filters.Filter
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.copyTo

object DatabaseConfigurationTestUtils {

    val DATABASE_FILE_V1_2_x = File(DatabaseConfigurationTestUtils::class.java.getResource("/persistence/v1.2.db")!!.toURI()).toPath()
    val DATABASE_FILE_V2_10_5 = File(DatabaseConfigurationTestUtils::class.java.getResource("/persistence/v2.10.5.db")!!.toURI()).toPath()
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getTestDatabase(fromTemplate: Path? = null): Nitrite {

        val databaseFile = Files.createTempFile("v-rising-bot", ".db").also {
            logger.info("Test Db location: " + it.absolutePathString())
        }

        if (fromTemplate != null) {
            logger.info("Loading template from '${fromTemplate.absolutePathString()}'.")
            fromTemplate.copyTo(databaseFile, overwrite = true)
        }

        return DatabaseConfiguration.buildNitriteDatabase(databaseFile)
    }

    fun clearDatabase(nitrite: Nitrite) {
        nitrite.listCollectionNames().forEach { collectionName ->
            nitrite.getCollection(collectionName).use {
                it.remove(Filter.ALL)
            }
        }
    }
}
