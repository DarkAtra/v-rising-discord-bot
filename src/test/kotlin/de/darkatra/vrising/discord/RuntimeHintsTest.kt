package de.darkatra.vrising.discord

import org.junit.jupiter.api.Test
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

    @ImportRuntimeHints(BotRuntimeHints::class, TestRuntimeHints::class)
    class TestConfiguration
}
