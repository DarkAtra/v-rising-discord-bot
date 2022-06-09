package de.darkatra.vrising.discord

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
    private var repository = database.getRepository(Schema::class.java)

    fun migrateToLatestVersion() {

        // find the current version or default to V1.3.0 (the version before this feature was introduced)
        val (currentSchemaMajor, currentSchemaMinor, currentSchemaPatch) = repository.find().toList()
            .maxByOrNull(Schema::appVersion)
            ?.let(Schema::appVersionAsSemanticVersion)
            ?: SemanticVersion(major = 1, minor = 3, patch = 0)

        // migrate from V1.3.0 to V1.4.x
        if (currentSchemaMajor == 1 && currentSchemaMinor == 3 && currentSchemaPatch == 0) {
            serverStatusMonitorService.getServerStatusMonitors().forEach { serverStatusMonitor ->
                val serverStatusMonitorBuilder = serverStatusMonitor.builder()
                serverStatusMonitorBuilder.displayPlayerGearLevel = true
                serverStatusMonitorService.putServerStatusMonitor(serverStatusMonitorBuilder.build())
            }

            repository.insert(Schema("V$currentAppVersion"))
            logger.info("Database migration from V$currentSchemaMajor.$currentSchemaMinor.$currentSchemaPatch to V$currentAppVersion was successful.")
        }
    }
}
