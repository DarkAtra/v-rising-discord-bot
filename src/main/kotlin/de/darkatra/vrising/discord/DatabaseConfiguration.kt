package de.darkatra.vrising.discord

import org.dizitart.no2.Nitrite
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.io.path.absolutePathString

@Configuration
@EnableConfigurationProperties(BotProperties::class)
class DatabaseConfiguration(
    private val botProperties: BotProperties
) {

    @Bean
    fun database(): Nitrite {
        return Nitrite.builder()
            .filePath(botProperties.databasePath.absolutePathString())
            .openOrCreate(botProperties.databaseUsername, botProperties.databasePassword)
    }
}
