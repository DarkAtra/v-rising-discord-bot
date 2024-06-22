package de.darkatra.vrising.discord.migration

import de.darkatra.vrising.discord.persistence.model.Status
import org.dizitart.no2.Document
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.filters.ObjectFilters
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
            documentCollectionName = "de.darkatra.vrising.discord.ServerStatusMonitor",
            documentAction = { document -> document["displayPlayerGearLevel"] = true }
        ),
        DatabaseMigration(
            description = "Set default value for status and displayServerDescription property.",
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major == 1 && currentSchemaVersion.minor <= 4 },
            documentCollectionName = "de.darkatra.vrising.discord.ServerStatusMonitor",
            documentAction = { document ->
                document["status"] = Status.ACTIVE.name
                document["displayServerDescription"] = true
            }
        ),
        DatabaseMigration(
            description = "Remove the displayPlayerGearLevel property due to patch 0.5.42405.",
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major == 1 && currentSchemaVersion.minor <= 5 },
            documentCollectionName = "de.darkatra.vrising.discord.ServerStatusMonitor",
            documentAction = { document ->
                // we can't remove the field completely due to how nitrites update function works
                // setting it to false instead (this was the default value in previous versions)
                document["displayPlayerGearLevel"] = false
            }
        ),
        DatabaseMigration(
            description = "Set default value for currentFailedAttempts property.",
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major == 1 && currentSchemaVersion.minor <= 7 },
            documentCollectionName = "de.darkatra.vrising.discord.ServerStatusMonitor",
            documentAction = { document -> document["currentFailedAttempts"] = 0 }
        ),
        DatabaseMigration(
            description = "Migrate the existing ServerStatusMonitor collection to the new collection name introduced by a package change and set defaults for displayClan, displayGearLevel and displayKilledVBloods.",
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major < 2 || (currentSchemaVersion.major == 2 && currentSchemaVersion.minor <= 1) },
            documentCollectionName = "de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor",
            databaseAction = { database ->
                val oldCollection = database.getCollection("de.darkatra.vrising.discord.ServerStatusMonitor")
                val newCollection = database.getCollection("de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor")
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
            documentCollectionName = "de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor",
            documentAction = { document ->
                document["version"] = Instant.now().toEpochMilli()
            }
        ),
        DatabaseMigration(
            description = "Make it possible to disable the discord embed and only use the activity or kill feed.",
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major < 2 || (currentSchemaVersion.major == 2 && currentSchemaVersion.minor <= 8) },
            documentCollectionName = "de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor",
            documentAction = { document ->
                document["embedEnabled"] = true
            }
        ),
        DatabaseMigration(
            description = "Serialize error timestamp as long (epochSecond).",
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major < 2 || (currentSchemaVersion.major == 2 && currentSchemaVersion.minor <= 9) },
            documentCollectionName = "de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor",
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
        ),
        DatabaseMigration(
            description = "Migrate the existing ServerStatusMonitor collection to the new collection name introduced by a package change.",
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major < 2 || (currentSchemaVersion.major == 2 && currentSchemaVersion.minor <= 10 && currentSchemaVersion.patch <= 1) },
            documentCollectionName = "de.darkatra.vrising.discord.persistence.model.ServerStatusMonitor",
            databaseAction = { database ->
                val oldCollection = database.getCollection("de.darkatra.vrising.discord.serverstatus.model.ServerStatusMonitor")
                val newCollection = database.getCollection("de.darkatra.vrising.discord.persistence.model.ServerStatusMonitor")
                oldCollection.find().forEach { document ->
                    newCollection.insert(document)
                }
                oldCollection.remove(ObjectFilters.ALL)
            }
        ),
        DatabaseMigration(
            description = "Each feature now has its own nested database object. Will not migrate previous errors to the new format.",
            isApplicable = { currentSchemaVersion -> currentSchemaVersion.major < 2 || (currentSchemaVersion.major == 2 && currentSchemaVersion.minor <= 10) },
            documentCollectionName = "de.darkatra.vrising.discord.persistence.model.Server",
            databaseAction = { database ->
                val oldCollection = database.getCollection("de.darkatra.vrising.discord.persistence.model.ServerStatusMonitor")
                val newCollection = database.getCollection("de.darkatra.vrising.discord.persistence.model.Server")
                oldCollection.find().forEach { document ->

                    val server = Document().apply {
                        put("id", document["id"])
                        put("version", document["version"])
                        put("discordServerId", document["discordServerId"])
                        put("hostname", document["hostname"])
                        put("queryPort", document["queryPort"])
                        put("apiHostname", document["apiHostname"])
                        put("apiPort", document["apiPort"])
                        put("apiUsername", document["apiUsername"])
                        put("apiPassword", document["apiPassword"])

                        if (document["playerActivityDiscordChannelId"] != null) {
                            put(
                                "playerActivityFeed",
                                mapOf(
                                    "status" to Status.ACTIVE,
                                    "discordChannelId" to document["playerActivityDiscordChannelId"]
                                )
                            )
                        }

                        if (document["pvpKillFeedDiscordChannelId"] != null) {
                            put(
                                "pvpKillFeed",
                                mapOf(
                                    "status" to Status.ACTIVE,
                                    "discordChannelId" to document["pvpKillFeedDiscordChannelId"]
                                )
                            )
                        }

                        if (document["embedEnabled"] == true) {
                            put(
                                "statusMonitor",
                                mapOf(
                                    "status" to document["status"],
                                    "discordChannelId" to document["discordChannelId"],
                                    "displayServerDescription" to document["displayServerDescription"],
                                    "displayPlayerGearLevel" to document["displayPlayerGearLevel"],
                                    "currentEmbedMessageId" to document["currentEmbedMessageId"],
                                    "currentFailedAttempts" to document["currentFailedAttempts"],
                                    "currentFailedApiAttempts" to document["currentFailedApiAttempts"],
                                )
                            )
                        }
                    }
                    newCollection.insert(server)
                }
                oldCollection.remove(ObjectFilters.ALL)
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
        migrationsToPerform.forEach { migration ->
            migration.databaseAction(database)

            val collection = database.getCollection(migration.documentCollectionName)
            collection.find().forEach { document ->
                migration.documentAction(document)
                collection.update(document)
            }
        }

        repository.insert(Schema("V$currentAppVersion"))
        logger.info("Database migration from V$currentSchemaVersion to V$currentAppVersion was successful.")

        return true
    }
}
