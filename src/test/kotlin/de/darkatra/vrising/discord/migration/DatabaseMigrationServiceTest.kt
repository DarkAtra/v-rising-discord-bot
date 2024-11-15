package de.darkatra.vrising.discord.migration

import de.darkatra.vrising.discord.DatabaseConfigurationTestUtils
import de.darkatra.vrising.discord.persistence.model.Server
import de.darkatra.vrising.discord.persistence.model.Status
import de.darkatra.vrising.discord.persistence.model.Version
import org.assertj.core.api.Assertions.assertThat
import org.dizitart.no2.collection.Document
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import java.time.Instant

@ExtendWith(OutputCaptureExtension::class)
class DatabaseMigrationServiceTest {

    @Test
    fun `should perform database migration when no schema was found`() {

        DatabaseConfigurationTestUtils.getTestDatabase().use { database ->

            val databaseMigrationService = DatabaseMigrationService(
                database = database,
                appVersionFromPom = "1.5.0"
            )

            assertThat(databaseMigrationService.migrateToLatestVersion()).isTrue()

            val repository = database.getRepository(Schema::class.java)

            val schemas = repository.find().toList()
            assertThat(schemas).hasSize(1)
            assertThat(schemas).first().extracting(Schema::appVersion).isEqualTo("V1.5.0")
        }
    }

    @Test
    fun `should not perform database migration when schema matches the current version`() {

        DatabaseConfigurationTestUtils.getTestDatabase().use { database ->

            database.getRepository(Schema::class.java).use { repository ->
                repository.insert(Schema(appVersion = "V1.4.0"))
                repository.insert(Schema(appVersion = "V1.5.0"))
                repository.insert(Schema(appVersion = "V1.6.0"))
                repository.insert(Schema(appVersion = "V1.8.0"))
                repository.insert(Schema(appVersion = "V2.2.0"))
                repository.insert(Schema(appVersion = "V2.3.0"))
                repository.insert(Schema(appVersion = "V2.9.0"))
                repository.insert(Schema(appVersion = "V2.10.0"))
                repository.insert(Schema(appVersion = "V2.10.2"))
                repository.insert(Schema(appVersion = "V2.11.0"))
            }

            val databaseMigrationService = DatabaseMigrationService(
                database = database,
                appVersionFromPom = "2.11.0"
            )

            assertThat(databaseMigrationService.migrateToLatestVersion()).isFalse()

            val schemas = database.getRepository(Schema::class.java).use { repository ->
                repository.find().toList()
            }
            assertThat(schemas).hasSize(10)
        }
    }

    @Test
    fun `should migrate existing ServerStatusMonitor documents to new collection and cleanup obsolete data`() {

        DatabaseConfigurationTestUtils.getTestDatabase().use { database ->

            database.getRepository(Schema::class.java).use { repository ->
                repository.insert(Schema(appVersion = "V2.1.0"))
            }

            val databaseMigrationService = DatabaseMigrationService(
                database = database,
                appVersionFromPom = "2.9.0"
            )

            database.getCollection("de.darkatra.vrising.discord.ServerStatusMonitor").use { oldCollection ->
                oldCollection.insert(Document.createDocument("hostName", "test-hostname"))
                assertThat(oldCollection.size()).isEqualTo(1)
            }

            database.getCollection("de.darkatra.vrising.discord.persistence.model.Server").use { newCollection ->
                assertThat(newCollection.size()).isEqualTo(0)
            }

            assertThat(databaseMigrationService.migrateToLatestVersion()).isTrue()

            database.getCollection("de.darkatra.vrising.discord.ServerStatusMonitor").use { oldCollection ->
                assertThat(oldCollection.size()).isEqualTo(0)
            }

            val migratedDocument = database.getCollection("de.darkatra.vrising.discord.persistence.model.Server").use { newCollection ->
                assertThat(newCollection.size()).isEqualTo(1)
                newCollection.find().first()
            }
            assertThat(migratedDocument["hostname"]).isEqualTo("test-hostname")

            val schemas = database.getRepository(Schema::class.java).use { repository ->
                repository.find().toList()
            }
            assertThat(schemas).hasSize(2)
        }
    }

    @Test
    fun `should migrate schema from 1_2_x to 2_11_0`() {

        DatabaseConfigurationTestUtils.getTestDatabase(DatabaseConfigurationTestUtils.DATABASE_FILE_V1_2_x).use { database ->

            val databaseMigrationService = DatabaseMigrationService(
                database = database,
                appVersionFromPom = "2.11.0"
            )

            val oldDocument = database.getCollection("de.darkatra.vrising.discord.ServerStatusMonitor").use { oldCollection ->
                assertThat(oldCollection.size()).isEqualTo(1)
                oldCollection.find().first()
            }
            database.getRepository(Server::class.java).use { serverRepository ->
                assertThat(serverRepository.size()).isEqualTo(0)
            }

            assertThat(databaseMigrationService.migrateToLatestVersion()).isTrue()

            database.getCollection("de.darkatra.vrising.discord.ServerStatusMonitor").use { oldCollection ->
                assertThat(oldCollection.size()).isEqualTo(0)
            }

            val server = database.getRepository(Server::class.java).use { serverRepository ->
                assertThat(serverRepository.size()).isEqualTo(1)
                serverRepository.find().first()
            }
            assertThat(server.id).isEqualTo(oldDocument["id"])
            @Suppress("DEPRECATION")
            assertThat(server.version).isNotNull()
            assertThat(server.discordServerId).isEqualTo(oldDocument["discordServerId"])
            assertThat(server.hostname).isEqualTo(oldDocument["hostName"])
            assertThat(server.queryPort).isEqualTo(oldDocument["queryPort"])
            assertThat(server.apiHostname).isEqualTo(oldDocument["apiHostname"])
            assertThat(server.apiPort).isEqualTo(oldDocument["apiPort"])
            assertThat(server.apiUsername).isEqualTo(oldDocument["apiUsername"])
            assertThat(server.apiPassword).isEqualTo(oldDocument["apiPassword"])
            assertThat(server.pvpLeaderboard).isNull()
            assertThat(server.playerActivityFeed).isNull()
            assertThat(server.pvpKillFeed).isNull()
            assertThat(server.statusMonitor).isNotNull()
            assertThat(server.statusMonitor!!.status).isNotNull()
            assertThat(server.statusMonitor!!.status).isEqualTo(Status.ACTIVE)
            assertThat(server.statusMonitor!!.discordChannelId).isEqualTo(oldDocument["discordChannelId"])
            assertThat(server.statusMonitor!!.displayServerDescription).isTrue()
            assertThat(server.statusMonitor!!.displayPlayerGearLevel).isTrue()
            assertThat(server.statusMonitor!!.currentEmbedMessageId).isEqualTo(oldDocument["currentEmbedMessageId"])
            assertThat(server.statusMonitor!!.currentFailedAttempts).isEqualTo(0)
            assertThat(server.statusMonitor!!.currentFailedApiAttempts).isEqualTo(0)
            assertThat(server.statusMonitor!!.recentErrors).isEmpty()
        }
    }

    @Test
    fun `should migrate schema from 2_10_5 to 2_11_0`() {

        DatabaseConfigurationTestUtils.getTestDatabase(DatabaseConfigurationTestUtils.DATABASE_FILE_V2_10_5).use { database ->

            val repository = database.getRepository(Schema::class.java)
            repository.insert(Schema(appVersion = "V2.10.5"))

            val databaseMigrationService = DatabaseMigrationService(
                database = database,
                appVersionFromPom = "2.11.0"
            )

            database.getRepository(Server::class.java).use { serverRepository ->
                assertThat(serverRepository.size()).isEqualTo(0)
            }

            val oldDocument = database.getCollection("de.darkatra.vrising.discord.persistence.model.ServerStatusMonitor").use { oldCollection ->
                assertThat(oldCollection.size()).isEqualTo(1)
                oldCollection.find().first()
            }

            assertThat(databaseMigrationService.migrateToLatestVersion()).isTrue()

            database.getCollection("de.darkatra.vrising.discord.persistence.model.ServerStatusMonitor").use { oldCollection ->
                assertThat(oldCollection.size()).isEqualTo(0)
            }

            val server = database.getRepository(Server::class.java).use { serverRepository ->
                assertThat(serverRepository.size()).isEqualTo(1)
                serverRepository.find().first()
            }
            assertThat(server.id).isEqualTo(oldDocument["id"])
            @Suppress("DEPRECATION")
            assertThat(server.version).isEqualTo(Version(1, Instant.ofEpochMilli(oldDocument["version"] as Long)))
            assertThat(server.discordServerId).isEqualTo(oldDocument["discordServerId"])
            assertThat(server.hostname).isEqualTo(oldDocument["hostname"])
            assertThat(server.queryPort).isEqualTo(oldDocument["queryPort"])
            assertThat(server.apiHostname).isEqualTo(oldDocument["apiHostname"])
            assertThat(server.apiPort).isEqualTo(oldDocument["apiPort"])
            assertThat(server.apiUsername).isEqualTo(oldDocument["apiUsername"])
            assertThat(server.apiPassword).isEqualTo(oldDocument["apiPassword"])
            assertThat(server.pvpLeaderboard).isNull()
            assertThat(server.playerActivityFeed).isNotNull()
            assertThat(server.playerActivityFeed!!.status).isEqualTo(Status.ACTIVE)
            assertThat(server.playerActivityFeed!!.discordChannelId).isEqualTo(oldDocument["playerActivityDiscordChannelId"])
            assertThat(server.playerActivityFeed!!.lastUpdated).isNotNull()
            assertThat(server.playerActivityFeed!!.currentFailedAttempts).isEqualTo(0)
            assertThat(server.playerActivityFeed!!.recentErrors).isEmpty()
            assertThat(server.pvpKillFeed).isNotNull()
            assertThat(server.pvpKillFeed!!.status).isEqualTo(Status.ACTIVE)
            assertThat(server.pvpKillFeed!!.discordChannelId).isEqualTo(oldDocument["pvpKillFeedDiscordChannelId"])
            assertThat(server.pvpKillFeed!!.lastUpdated).isNotNull()
            assertThat(server.pvpKillFeed!!.currentFailedAttempts).isEqualTo(0)
            assertThat(server.pvpKillFeed!!.recentErrors).isEmpty()
            assertThat(server.statusMonitor).isNotNull()
            assertThat(server.statusMonitor!!.status).isNotNull()
            assertThat(server.statusMonitor!!.status).isEqualTo(Status.ACTIVE)
            assertThat(server.statusMonitor!!.discordChannelId).isEqualTo(oldDocument["discordChannelId"])
            assertThat(server.statusMonitor!!.displayServerDescription).isEqualTo(oldDocument["displayServerDescription"])
            assertThat(server.statusMonitor!!.displayPlayerGearLevel).isEqualTo(oldDocument["displayPlayerGearLevel"])
            assertThat(server.statusMonitor!!.currentEmbedMessageId).isEqualTo(oldDocument["currentEmbedMessageId"])
            assertThat(server.statusMonitor!!.currentFailedAttempts).isEqualTo(oldDocument["currentFailedAttempts"])
            assertThat(server.statusMonitor!!.currentFailedApiAttempts).isEqualTo(oldDocument["currentFailedApiAttempts"])
            assertThat(server.statusMonitor!!.recentErrors).isEmpty()
        }
    }

    @Test
    fun `should migrate schema of password secured database from 2_10_5 to 2_11_0`(capturedOutput: CapturedOutput) {

        DatabaseConfigurationTestUtils.getTestDatabase(DatabaseConfigurationTestUtils.DATABASE_FILE_V2_10_5_WITH_PASSWORD, "test", "test").use { database ->

            assertThat(capturedOutput.out).contains("Successfully encrypted the database.")

            val repository = database.getRepository(Schema::class.java)
            repository.insert(Schema(appVersion = "V2.10.5"))

            val databaseMigrationService = DatabaseMigrationService(
                database = database,
                appVersionFromPom = "2.11.0"
            )

            database.getRepository(Server::class.java).use { serverRepository ->
                assertThat(serverRepository.size()).isEqualTo(0)
            }

            val oldDocument = database.getCollection("de.darkatra.vrising.discord.persistence.model.ServerStatusMonitor").use { oldCollection ->
                assertThat(oldCollection.size()).isEqualTo(1)
                oldCollection.find().first()
            }

            assertThat(databaseMigrationService.migrateToLatestVersion()).isTrue()

            database.getCollection("de.darkatra.vrising.discord.persistence.model.ServerStatusMonitor").use { oldCollection ->
                assertThat(oldCollection.size()).isEqualTo(0)
            }

            val server = database.getRepository(Server::class.java).use { serverRepository ->
                assertThat(serverRepository.size()).isEqualTo(1)
                serverRepository.find().first()
            }
            assertThat(server.id).isEqualTo(oldDocument["id"])
            @Suppress("DEPRECATION")
            assertThat(server.version).isEqualTo(Version(1, Instant.ofEpochMilli(oldDocument["version"] as Long)))
            assertThat(server.discordServerId).isEqualTo(oldDocument["discordServerId"])
            assertThat(server.hostname).isEqualTo(oldDocument["hostname"])
            assertThat(server.queryPort).isEqualTo(oldDocument["queryPort"])
            assertThat(server.apiHostname).isEqualTo(oldDocument["apiHostname"])
            assertThat(server.apiPort).isEqualTo(oldDocument["apiPort"])
            assertThat(server.apiUsername).isEqualTo(oldDocument["apiUsername"])
            assertThat(server.apiPassword).isEqualTo(oldDocument["apiPassword"])
            assertThat(server.pvpLeaderboard).isNull()
            assertThat(server.playerActivityFeed).isNotNull()
            assertThat(server.playerActivityFeed!!.status).isEqualTo(Status.ACTIVE)
            assertThat(server.playerActivityFeed!!.discordChannelId).isEqualTo(oldDocument["playerActivityDiscordChannelId"])
            assertThat(server.playerActivityFeed!!.lastUpdated).isNotNull()
            assertThat(server.playerActivityFeed!!.currentFailedAttempts).isEqualTo(0)
            assertThat(server.playerActivityFeed!!.recentErrors).isEmpty()
            assertThat(server.pvpKillFeed).isNotNull()
            assertThat(server.pvpKillFeed!!.status).isEqualTo(Status.ACTIVE)
            assertThat(server.pvpKillFeed!!.discordChannelId).isEqualTo(oldDocument["pvpKillFeedDiscordChannelId"])
            assertThat(server.pvpKillFeed!!.lastUpdated).isNotNull()
            assertThat(server.pvpKillFeed!!.currentFailedAttempts).isEqualTo(0)
            assertThat(server.pvpKillFeed!!.recentErrors).isEmpty()
            assertThat(server.statusMonitor).isNotNull()
            assertThat(server.statusMonitor!!.status).isNotNull()
            assertThat(server.statusMonitor!!.status).isEqualTo(Status.ACTIVE)
            assertThat(server.statusMonitor!!.discordChannelId).isEqualTo(oldDocument["discordChannelId"])
            assertThat(server.statusMonitor!!.displayServerDescription).isEqualTo(oldDocument["displayServerDescription"])
            assertThat(server.statusMonitor!!.displayPlayerGearLevel).isEqualTo(oldDocument["displayPlayerGearLevel"])
            assertThat(server.statusMonitor!!.currentEmbedMessageId).isEqualTo(oldDocument["currentEmbedMessageId"])
            assertThat(server.statusMonitor!!.currentFailedAttempts).isEqualTo(oldDocument["currentFailedAttempts"])
            assertThat(server.statusMonitor!!.currentFailedApiAttempts).isEqualTo(oldDocument["currentFailedApiAttempts"])
            assertThat(server.statusMonitor!!.recentErrors).isEmpty()
        }
    }
}
