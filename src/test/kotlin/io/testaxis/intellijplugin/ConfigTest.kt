package io.testaxis.intellijplugin

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isNotEmpty

class ConfigTest {
    @Test
    fun `it provides a helper function to easily retrieve configuration values`() {
        val configValue = config(config.testaxis.ws.topics.builds)

        expectThat(configValue).isNotEmpty()
    }
}
