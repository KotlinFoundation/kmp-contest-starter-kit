package com.kotlinfoundation.koko.util.extensions

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class DateTimeFormatExtTest {
    private val utc = TimeZone.UTC

    // 2026-03-07T12:00:00Z
    private val epochMillis = 1_772_884_800_000L

    @Test
    fun `formats with the default pattern`() {
        assertEquals("07.03.2026", epochMillis.asFormattedDate(timeZone = utc))
    }

    @Test
    fun `pads day and month to two digits`() {
        assertEquals("2026-03-07", epochMillis.asFormattedDate(timeZone = utc, format = "yyyy-MM-dd"))
    }
}
