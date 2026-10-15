package com.bioscanner.data.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GoogleNonceTest {

    @Test
    fun `hashed is the sha256 hex of raw`() {
        // Vetor conhecido: SHA-256("abc").
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            GoogleNonce.sha256Hex("abc"),
        )
        val nonce = GoogleNonce.create()
        assertEquals(GoogleNonce.sha256Hex(nonce.raw), nonce.hashed)
        assertEquals(64, nonce.raw.length)
    }

    @Test
    fun `each nonce is different`() {
        assertNotEquals(GoogleNonce.create().raw, GoogleNonce.create().raw)
    }
}
