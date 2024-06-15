package de.darkatra.vrising.discord

import de.darkatra.vrising.discord.migration.Schema
import de.darkatra.vrising.discord.persistence.model.ServerStatusMonitor
import org.junit.jupiter.api.Test
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ImportRuntimeHints

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = [RuntimeHintsTest.TestConfiguration::class]
)
class RuntimeHintsTest {

    @Test
    fun generateRuntimeHints() {
        // workaround to generate runtime hints for unit tests
    }

    @ImportRuntimeHints(BotRuntimeHints::class)
    @RegisterReflectionForBinding(BotProperties::class, Schema::class, ServerStatusMonitor::class)
    class TestConfiguration
}
