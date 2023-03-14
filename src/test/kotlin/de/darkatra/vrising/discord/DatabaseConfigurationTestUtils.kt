package de.darkatra.vrising.discord

import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.filters.ObjectFilters
import org.slf4j.LoggerFactory
import java.io.File

object DatabaseConfigurationTestUtils {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun getTestDatabase(): Nitrite {
        return Nitrite.builder()
            .compressed()
            .filePath(File.createTempFile("v-rising-bot", ".db").also {
                logger.info("Test Db location: " + it.absolutePath)
            })
            .openOrCreate()
    }

    fun clearDatabase(nitrite: Nitrite) {
        nitrite.listCollectionNames().forEach { collectionName ->
            nitrite.getCollection(collectionName).remove(ObjectFilters.ALL)
        }
    }
}
