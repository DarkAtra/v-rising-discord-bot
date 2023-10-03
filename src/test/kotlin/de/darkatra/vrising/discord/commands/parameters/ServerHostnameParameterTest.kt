package de.darkatra.vrising.discord.commands.parameters

import de.darkatra.vrising.discord.commands.exceptions.ValidationException
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ServerHostnameParameterTest {

    @ParameterizedTest
    @ValueSource(strings = ["localhost", "darkatra.de"])
    fun `should permit hostnames`(hostname: String) {
        assertDoesNotThrow {
            ServerHostnameParameter.validate(hostname, true)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["127.0.0.1", "192.168.178.1"])
    fun `should permit ip v4 addresses`(ipv4: String) {
        assertDoesNotThrow {
            ServerHostnameParameter.validate(ipv4, true)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["0:0:0:0:0:0:0:1", "::1"])
    fun `should permit ip v6 addresses`(ipv6: String) {
        assertDoesNotThrow {
            ServerHostnameParameter.validate(ipv6, true)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["DarkAtra.de - V Rising Duo PvP", "[EU] Official #1234"])
    fun `should reject server names`(hostname: String) {
        assertThrows<ValidationException> {
            ServerHostnameParameter.validate(hostname, false)
        }
    }
}
