package de.darkatra.vrising.discord.persistence

import de.darkatra.vrising.discord.BotProperties
import de.darkatra.vrising.discord.migration.SchemaEntityConverter
import de.darkatra.vrising.discord.persistence.model.converter.ErrorEntityConverter
import de.darkatra.vrising.discord.persistence.model.converter.PlayerActivityFeedEntityConverter
import de.darkatra.vrising.discord.persistence.model.converter.PvpKillFeedEntityConverter
import de.darkatra.vrising.discord.persistence.model.converter.ServerEntityConverter
import de.darkatra.vrising.discord.persistence.model.converter.StatusMonitorEntityConverter
import org.dizitart.no2.Nitrite
import org.dizitart.no2.mvstore.MVStoreModule
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Path
import kotlin.io.path.absolutePathString

@Configuration
@EnableConfigurationProperties(BotProperties::class)
class DatabaseConfiguration(
    private val botProperties: BotProperties
) {

    companion object {

        fun buildNitriteDatabase(databaseFile: Path, username: String? = null, password: String? = null): Nitrite {

            val storeModule = MVStoreModule.withConfig()
                .filePath(databaseFile.absolutePathString())
                .compress(true)
                .build()

            return Nitrite.builder()
                .loadModule(storeModule)
                .disableRepositoryTypeValidation()
                .registerEntityConverter(SchemaEntityConverter())
                .registerEntityConverter(ErrorEntityConverter())
                .registerEntityConverter(PlayerActivityFeedEntityConverter())
                .registerEntityConverter(PvpKillFeedEntityConverter())
                .registerEntityConverter(ServerEntityConverter())
                .registerEntityConverter(StatusMonitorEntityConverter())
                .openOrCreate(username, password)
        }
    }

    @Bean
    fun database(): Nitrite {

        return buildNitriteDatabase(
            botProperties.databasePath,
            botProperties.databaseUsername,
            botProperties.databasePassword
        )
    }
}
