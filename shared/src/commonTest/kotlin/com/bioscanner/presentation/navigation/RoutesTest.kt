package com.bioscanner.presentation.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class RoutesTest {

    @Test
    fun `photo uri is encoded like Uri encode`() {
        assertEquals(
            "content%3A%2F%2Fmedia%2Fexternal%2Fimages%2Fmedia%2F42",
            encodeRouteArgument("content://media/external/images/media/42"),
        )
        assertEquals("a%20b%C3%A7", encodeRouteArgument("a bç"))
        assertEquals("keep-_.!~*'()", encodeRouteArgument("keep-_.!~*'()"))
    }
}
