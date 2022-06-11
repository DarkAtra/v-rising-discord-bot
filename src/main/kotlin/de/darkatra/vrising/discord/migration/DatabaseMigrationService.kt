package de.darkatra.vrising.discord.migration

import de.darkatra.vrising.discord.ServerStatusMonitorService
import de.darkatra.vrising.discord.ServerStatusMonitorStatus
import org.dizitart.no2.Nitrite
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class DatabaseMigrationService(
    database: Nitrite,
    private val serverStatusMonitorService: ServerStatusMonitorService,
    @Value("\${version}")
    private val currentAppVersion: String
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val repository = database.getRepository(Schema::class.java)

    private val migrations: List<DatabaseMigration> = listOf(
        DatabaseMigration(
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major == 1 && currentSchemaVersion.minor <= 3 },
            action = { serverStatusMonitorBuilder -> serverStatusMonitorBuilder.displayPlayerGearLevel = true }
        ),
        DatabaseMigration(
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major == 1 && currentSchemaVersion.minor <= 4 },
            action = { serverStatusMonitorBuilder -> serverStatusMonitorBuilder.status = ServerStatusMonitorStatus.ACTIVE }
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

            serverStatusMonitorService.getServerStatusMonitors().forEach { serverStatusMonitor ->
                val serverStatusMonitorBuilder = serverStatusMonitor.builder()
                migrationsToPerform.forEach { migration -> migration.action(serverStatusMonitorBuilder) }
                serverStatusMonitorService.putServerStatusMonitor(serverStatusMonitorBuilder.build())
            }

            repository.insert(Schema("V$currentAppVersion"))
            logger.info("Database migration from V$major.$minor.$patch to V$currentAppVersion was successful.")
        } else {
            logger.info("No migrations need to be performed.")
        }
    }
}
