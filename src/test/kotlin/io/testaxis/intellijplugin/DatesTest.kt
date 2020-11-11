package io.testaxis.intellijplugin

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.Calendar

class DatesTest {
    @Test
    fun `it provides a helper that formats the date now`() {
        val date = aFewSecondsAgo()

        expectThat(date.diffForHumans()).isEqualTo("moments ago")
    }

    @Test
    fun `it provides a helper that formats the date one hour ago`() {
        val date = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, get(Calendar.HOUR_OF_DAY) - 1)
        }.time

        expectThat(date.diffForHumans()).isEqualTo("1 hour ago")
    }
}
