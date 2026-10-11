package com.bioscanner.domain.model

import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.Test

class RegionTest {

    private fun square(x0: Float, y0: Float, x1: Float, y1: Float) =
        floatArrayOf(x0, y0, x1, y0, x1, y1, x0, y1, x0, y0)

    @Test
    fun `point inside a simple polygon is contained`() {
        val region = Region("A", "A", listOf(square(0f, 0f, 10f, 10f)))
        assertTrue(region.contains(5f, 5f))
    }

    @Test
    fun `point outside a simple polygon is not contained`() {
        val region = Region("A", "A", listOf(square(0f, 0f, 10f, 10f)))
        assertFalse(region.contains(15f, 5f))
        assertFalse(region.contains(5f, -1f))
    }

    @Test
    fun `point inside a hole is not contained by even-odd rule`() {
        // Como Goiás com o DF "furado" no meio.
        val region = Region("GO", "Goiás", listOf(square(0f, 0f, 10f, 10f), square(4f, 4f, 6f, 6f)))
        assertFalse(region.contains(5f, 5f))
        assertTrue(region.contains(2f, 2f))
    }

    @Test
    fun `point inside any part of a multi-polygon is contained`() {
        val region = Region("X", "Ilhas", listOf(square(0f, 0f, 1f, 1f), square(5f, 5f, 6f, 6f)))
        assertTrue(region.contains(0.5f, 0.5f))
        assertTrue(region.contains(5.5f, 5.5f))
        assertFalse(region.contains(3f, 3f))
    }
}
