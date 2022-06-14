package de.darkatra.vrising.discord.migration

import de.darkatra.vrising.discord.ServerStatusMonitor
import de.darkatra.vrising.discord.ServerStatusMonitorStatus
import org.dizitart.no2.Nitrite
import org.dizitart.no2.util.ObjectUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class DatabaseMigrationService(
    private val database: Nitrite,
    @Value("\${version}")
    private val currentAppVersion: String
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val repository = database.getRepository(Schema::class.java)

    private val migrations: List<DatabaseMigration> = listOf(
        DatabaseMigration(
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major == 1 && currentSchemaVersion.minor <= 3 },
            action = { document -> document["displayPlayerGearLevel"] = true }
        ),
        DatabaseMigration(
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major == 1 && currentSchemaVersion.minor <= 4 },
            action = { document ->
                document["status"] = ServerStatusMonitorStatus.ACTIVE.name
                document["displayServerDescription"] = true
            }
        )
    )

    fun migrateToLatestVersion() {

        // find the current version or default to V1.3.0 (the version before this feature was introduced)
        val currentSchemaVersion = repository.find().toList()
            .maxByOrNull(Schema::appVersion)
            ?.let(Schema::appVersionAsSemanticVersion)
            ?: SemanticVersion(major = 1, minor = 3, patch = 0)

        val migrationsToPerform = migrations.filter { migration -> migration.isApplicable(currentSchemaVersion) }
        if (migrationsToPerform.isNotEmpty()) {

            val (major, minor, patch) = currentSchemaVersion
            logger.info("Will migrate from V$major.$minor.$patch to V$currentAppVersion by performing ${migrationsToPerform.size} migrations.")

            val collection = database.getCollection(ObjectUtils.findObjectStoreName(ServerStatusMonitor::class.java))

            collection.find().forEach { document ->
                migrationsToPerform.forEach { migration -> migration.action(document) }
                collection.update(document)
            }

            repository.insert(Schema("V$currentAppVersion"))
            logger.info("Database migration from V$major.$minor.$patch to V$currentAppVersion was successful.")
        } else {
            logger.info("No migrations need to be performed.")
        }
    }
}
