package com.bioscanner.data.auth

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Nonce do login com Google: o Google recebe o [hashed] (SHA-256 em hex) e o embute no ID token;
 * o Supabase recebe o [raw], calcula o hash e compara. Isso impede reaproveitar um token roubado.
 */
class GoogleNonce private constructor(val raw: String, val hashed: String) {
    companion object {
        fun create(random: SecureRandom = SecureRandom()): GoogleNonce {
            val raw = ByteArray(32).also(random::nextBytes).toHex()
            return GoogleNonce(raw, sha256Hex(raw))
        }

        internal fun sha256Hex(value: String): String =
            MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).toHex()

        private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
    }
}
