package com.bioscanner.presentation.common

import com.bioscanner.domain.model.GeoPoint
import kotlin.test.Test
import kotlin.test.assertEquals

class FormattingTest {

    @Test
    fun `coordinates use five decimals and a dot`() {
        assertEquals("-15.60000, -56.10000", formatCoordinates(GeoPoint(-15.6, -56.1)))
        assertEquals("39.23726, -123.15003", formatCoordinates(GeoPoint(39.237256, -123.150034)))
    }

    @Test
    fun `rounding carries into the integer part`() {
        assertEquals("1.00000", formatDecimal(0.999999, 5))
        assertEquals("0.00000", formatDecimal(-0.000001, 5))
    }
}
