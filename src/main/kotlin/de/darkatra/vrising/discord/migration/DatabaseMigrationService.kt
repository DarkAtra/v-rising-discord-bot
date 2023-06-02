package de.darkatra.vrising.discord.migration

import de.darkatra.vrising.discord.serverstatus.ServerStatusMonitor
import de.darkatra.vrising.discord.serverstatus.ServerStatusMonitorStatus
import org.dizitart.no2.Nitrite
import org.dizitart.no2.util.ObjectUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

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
            description = "Set default value for new displayPlayerGearLevel property.",
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major == 1 && currentSchemaVersion.minor <= 3 },
            documentAction = { document -> document["displayPlayerGearLevel"] = true }
        ),
        DatabaseMigration(
            description = "Set default value for new status and displayServerDescription property.",
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
            description = "Set default value for new currentFailedAttempts property.",
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major == 1 && currentSchemaVersion.minor <= 7 },
            documentAction = { document -> document["currentFailedAttempts"] = 0 }
        ),
        DatabaseMigration(
            description = "Migrate the existing ServerStatusMonitor collection to the new collection name introduced by a package change.",
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major < 2 || (currentSchemaVersion.major == 2 && currentSchemaVersion.minor <= 1) },
            databaseAction = { database ->
                val collection = database.getCollection(ObjectUtils.findObjectStoreName(ServerStatusMonitor::class.java))
                database.getCollection("de.darkatra.vrising.discord.ServerStatusMonitor").find().forEach { document ->
                    collection.insert(document)
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
            logger.info("No migrations need to be performed.")
            return false
        }

        logger.info("Will migrate from V$currentSchemaVersion to V$currentAppVersion by performing ${migrationsToPerform.size} migrations.")

        val collection = database.getCollection(ObjectUtils.findObjectStoreName(ServerStatusMonitor::class.java))

        // perform migration that affect the whole database
        migrationsToPerform.forEach { migration -> migration.databaseAction(database) }

        // perform migration that affect documents in the ServerStatusMonitor collection
        collection.find().forEach { document ->
            migrationsToPerform.forEach { migration -> migration.documentAction(document) }
            collection.update(document)
        }

        repository.insert(Schema("V$currentAppVersion"))
        logger.info("Database migration from V$currentSchemaVersion to V$currentAppVersion was successful.")

        return true
    }
}
