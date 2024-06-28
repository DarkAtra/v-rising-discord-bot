package de.darkatra.vrising.discord

import de.darkatra.vrising.discord.persistence.DatabaseConfiguration
import org.dizitart.no2.Nitrite
import org.dizitart.no2.filters.Filter
import org.slf4j.LoggerFactory
import java.io.File

object DatabaseConfigurationTestUtils {

    val DATABASE_FILE_V1_2_x = File(DatabaseConfigurationTestUtils::class.java.getResource("/persistence/v1.2.db")!!.toURI())
    val DATABASE_FILE_V2_10_5 = File(DatabaseConfigurationTestUtils::class.java.getResource("/persistence/v2.10.5.db")!!.toURI())
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getTestDatabase(fromTemplate: File? = null): Nitrite {

        val databaseFile = File.createTempFile("v-rising-bot", ".db").also {
            logger.info("Test Db location: " + it.absolutePath)
        }

        if (fromTemplate != null) {
            logger.info("Loading template from '${fromTemplate.absolutePath}'.")
            fromTemplate.copyTo(databaseFile, overwrite = true)
        }

        return DatabaseConfiguration.buildNitriteDatabase(databaseFile.toPath())
    }

    fun clearDatabase(nitrite: Nitrite) {
        nitrite.listCollectionNames().forEach { collectionName ->
            nitrite.getCollection(collectionName).use {
                it.remove(Filter.ALL)
            }
        }
    }
}
