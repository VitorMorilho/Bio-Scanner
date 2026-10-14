package com.bioscanner.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GitHubUpdateCheckerTest {

    private fun release(tag: String, draft: Boolean = false, apk: Boolean = true) = GitHubRelease(
        tagName = tag,
        draft = draft,
        assets = if (apk) listOf(GitHubAsset("BioScanner-$tag.apk", "https://x/$tag.apk")) else emptyList(),
    )

    @Test
    fun `parses tags with prefix and suffix`() {
        assertEquals(listOf(1, 0), parseVersion("v1.0-teste"))
        assertEquals(listOf(1, 10, 2), parseVersion("1.10.2"))
        assertNull(parseVersion("teste"))
        assertNull(parseVersion(""))
    }

    @Test
    fun `compares numerically and ignores trailing zeros`() {
        assertEquals(1, compareVersions(listOf(1, 10), listOf(1, 9)).coerceIn(-1, 1))
        assertEquals(0, compareVersions(listOf(1, 0), listOf(1, 0, 0)))
    }

    @Test
    fun `picks the highest newer release with an apk`() {
        val update = pickUpdate(
            listOf(release("v1.0-teste"), release("v1.2-teste"), release("v1.3-teste", draft = true), release("v1.4", apk = false), release("v1.1-teste")),
            currentVersionName = "1.0",
        )
        assertEquals("1.2", update?.versionName)
        assertEquals("https://x/v1.2-teste.apk", update?.downloadUrl)
    }

    @Test
    fun `no update when already on the latest`() {
        assertNull(pickUpdate(listOf(release("v1.0-teste")), currentVersionName = "1.0"))
        assertNull(pickUpdate(emptyList(), currentVersionName = "1.0"))
    }
}
