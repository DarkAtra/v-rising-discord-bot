package de.darkatra.vrising.discord.migration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SchemaTest {

    @Test
    fun `should convert to semantic version`() {

        val schema = Schema(
            appVersion = "V1.4.2"
        )

        val semanticVersion = schema.asSemanticVersion()

        assertThat(semanticVersion.major).isEqualTo(1)
        assertThat(semanticVersion.minor).isEqualTo(4)
        assertThat(semanticVersion.patch).isEqualTo(2)
    }

    @Test
    fun `should convert to semantic version for pre-release`() {

        val schema = Schema(
            appVersion = "V1.4.2-next.1"
        )

        val semanticVersion = schema.asSemanticVersion()

        assertThat(semanticVersion.major).isEqualTo(1)
        assertThat(semanticVersion.minor).isEqualTo(4)
        assertThat(semanticVersion.patch).isEqualTo(2)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "     ", "V1", "V1.", "V1.4", "V1.4.", "a1.4.2", "a.4.2", "V1.b.2", "V1.4.c"])
    fun `should fail to convert to semantic version`(invalidAppVersion: String) {

        val schema = Schema(
            appVersion = invalidAppVersion
        )

        assertThrows<IllegalStateException> {
            schema.asSemanticVersion()
        }
    }
}
