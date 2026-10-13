package com.bioscanner.data.auth

import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.ExperimentalTime

// O construtor do UserInfo tem campos kotlin.time.Instant (ainda experimental).
@OptIn(ExperimentalTime::class)
class SupabaseUserMapperTest {

    private fun user(email: String?, vararg metadata: Pair<String, String>) = UserInfo(
        id = "u1",
        aud = "authenticated",
        email = email,
        userMetadata = buildJsonObject { metadata.forEach { (k, v) -> put(k, v) } },
    )

    @Test
    fun `manual sign up uses display_name`() {
        assertEquals("Ana", user("ana@x.com", "display_name" to " Ana ", "full_name" to "Outro").toAuthUser().displayName)
    }

    @Test
    fun `google account falls back to full_name then name`() {
        assertEquals("Ana Souza", user("ana@x.com", "full_name" to "Ana Souza", "name" to "Ana").toAuthUser().displayName)
        assertEquals("Ana", user("ana@x.com", "display_name" to " ", "name" to "Ana").toAuthUser().displayName)
    }

    @Test
    fun `without metadata uses the email prefix`() {
        val mapped = user("ana.souza@x.com").toAuthUser()

        assertEquals("ana.souza", mapped.displayName)
        assertEquals("u1", mapped.id)
        assertEquals("ana.souza@x.com", mapped.email)
    }
}
