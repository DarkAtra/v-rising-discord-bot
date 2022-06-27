package de.darkatra.vrising.discord.test

import org.dizitart.no2.Nitrite
import org.slf4j.LoggerFactory
import java.io.File

internal object DatabaseTestUtils {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun getTestDatabase(): Nitrite {
        return Nitrite.builder()
            .compressed()
            .filePath(File.createTempFile("v-rising-bot", ".db").also {
                logger.info("Test Db location: " + it.absolutePath)
            })
            .openOrCreate()
    }
}
