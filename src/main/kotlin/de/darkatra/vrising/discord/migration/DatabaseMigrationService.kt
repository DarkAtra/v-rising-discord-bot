package de.darkatra.vrising.discord.migration

import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor
import de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitorStatus
import org.dizitart.no2.Document
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.filters.ObjectFilters
import org.dizitart.no2.util.ObjectUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class DatabaseMigrationService(
    private val database: Nitrite,
    @Value("\${version}")
    appVersionFromPom: String,
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val repository = database.getRepository(Schema::class.java)

    private val currentAppVersion: SemanticVersion = Schema("V$appVersionFromPom").asSemanticVersion()
    private val migrations: List<DatabaseMigration> = listOf(
        DatabaseMigration(
            description = "Set default value for displayPlayerGearLevel property.",
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major == 1 && currentSchemaVersion.minor <= 3 },
            documentAction = { document -> document["displayPlayerGearLevel"] = true }
        ),
        DatabaseMigration(
            description = "Set default value for status and displayServerDescription property.",
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major == 1 && currentSchemaVersion.minor <= 4 },
            documentAction = { document ->
                document["status"] = ServerStatusMonitorStatus.ACTIVE.name
                document["displayServerDescription"] = true
            }
        ),
        DatabaseMigration(
            description = "Remove the displayPlayerGearLevel property due to patch 0.5.42405.",
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major == 1 && currentSchemaVersion.minor <= 5 },
            documentAction = { document ->
                // we can't remove the field completely due to how nitrites update function works
                // setting it to false instead (this was the default value in previous versions)
                document["displayPlayerGearLevel"] = false
            }
        ),
        DatabaseMigration(
            description = "Set default value for currentFailedAttempts property.",
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major == 1 && currentSchemaVersion.minor <= 7 },
            documentAction = { document -> document["currentFailedAttempts"] = 0 }
        ),
        DatabaseMigration(
            description = "Migrate the existing ServerStatusMonitor collection to the new collection name introduced by a package change and set defaults for displayClan, displayGearLevel and displayKilledVBloods.",
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major < 2 || (currentSchemaVersion.major == 2 && currentSchemaVersion.minor <= 1) },
            databaseAction = { database ->
                val oldCollection = database.getCollection("de.darkatra.vrising.discord.ServerStatusMonitor")
                val newCollection = database.getCollection(ObjectUtils.findObjectStoreName(ServerStatusMonitor::class.java))
                oldCollection.find().forEach { document ->
                    newCollection.insert(document)
                }
                oldCollection.remove(ObjectFilters.ALL)
            },
            documentAction = { document ->
                document["hostname"] = document["hostName"]
                document["displayPlayerGearLevel"] = true
            }
        ),
        DatabaseMigration(
            description = "Set default value for version property.",
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major < 2 || (currentSchemaVersion.major == 2 && currentSchemaVersion.minor <= 2) },
            documentAction = { document ->
                document["version"] = Instant.now().toEpochMilli()
            }
        ),
        DatabaseMigration(
            description = "Make it possible to disable the discord embed and only use the activity or kill feed.",
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major < 2 || (currentSchemaVersion.major == 2 && currentSchemaVersion.minor <= 8) },
            documentAction = { document ->
                document["embedEnabled"] = true
            }
        ),
        DatabaseMigration(
            description = "Serialize error timestamp as long (epochSecond).",
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major < 2 || (currentSchemaVersion.major == 2 && currentSchemaVersion.minor <= 9) },
            documentAction = { document ->
                val recentErrors = document["recentErrors"]
                if (recentErrors is List<*>) {
                    recentErrors.filterIsInstance<Document>().forEach { error ->
                        if (error["timestamp"] is String) {
                            error["timestamp"] = Instant.parse(error["timestamp"] as String).epochSecond
                        }
                    }
                } else {
                    document["recentErrors"] = emptyList<Any>()
                }
            }
        )
    )

    fun migrateToLatestVersion(): Boolean {

        // find the current version or default to V1.3.0 (the version before this feature was introduced)
        val currentSchemaVersion = repository.find().toList()
            .map(Schema::asSemanticVersion)
            .maxWithOrNull(SemanticVersion.getComparator())
            ?: SemanticVersion(major = 1, minor = 3, patch = 0)

        val migrationsToPerform = migrations.filter { migration -> migration.isApplicable(currentSchemaVersion) }
        if (migrationsToPerform.isEmpty()) {
            logger.info("No migrations need to be performed (V$currentSchemaVersion to V$currentAppVersion).")
            return false
        }

        logger.info("Will migrate from V$currentSchemaVersion to V$currentAppVersion by performing ${migrationsToPerform.size} migrations.")
        migrationsToPerform.forEachIndexed { index, migration ->
            logger.info("* $index: ${migration.description}")
        }

        // perform migration that affect the whole database
        migrationsToPerform.forEach { migration -> migration.databaseAction(database) }

        // perform migration that affect documents in the ServerStatusMonitor collection
        val collection = database.getCollection(ObjectUtils.findObjectStoreName(ServerStatusMonitor::class.java))
        collection.find().forEach { document ->
            migrationsToPerform.forEach { migration -> migration.documentAction(document) }
            collection.update(document)
        }

        repository.insert(Schema("V$currentAppVersion"))
        logger.info("Database migration from V$currentSchemaVersion to V$currentAppVersion was successful.")

        return true
    }
}
