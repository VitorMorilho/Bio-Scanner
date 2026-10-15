package com.bioscanner.data.update

import android.util.Log
import com.bioscanner.domain.update.AppUpdate
import com.bioscanner.domain.update.UpdateChecker
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val TAG = "GitHubUpdateChecker"

@Serializable
internal data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    val draft: Boolean = false,
    val assets: List<GitHubAsset> = emptyList(),
)

@Serializable
internal data class GitHubAsset(
    val name: String,
    @SerialName("browser_download_url") val downloadUrl: String,
)

/**
 * Procura versões novas nas Releases do GitHub (o APK de teste é distribuído por lá, fora da
 * Play Store). Usa a lista de releases e não `/releases/latest` porque esta ignora
 * pré-lançamentos, e as versões de teste são publicadas como pré-lançamento.
 *
 * @param repository "dono/repo"; vazio desliga a busca (builds de debug).
 */
class GitHubUpdateChecker(private val repository: String) : UpdateChecker {

    private val json = Json { ignoreUnknownKeys = true }

    private val client by lazy {
        HttpClient(OkHttp) {
            install(HttpTimeout) { requestTimeoutMillis = 10_000 }
            expectSuccess = false
        }
    }

    override suspend fun findUpdate(currentVersionName: String): AppUpdate? {
        if (repository.isBlank()) return null
        return try {
            val response = client.get("https://api.github.com/repos/$repository/releases?per_page=20") {
                header("Accept", "application/vnd.github+json")
            }
            // Ex.: limite da API sem autenticação (60/hora por IP). Tenta na próxima abertura.
            if (!response.status.isSuccess()) return null
            pickUpdate(json.decodeFromString(response.bodyAsText()), currentVersionName)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Sem internet ou resposta inesperada: o aviso é só uma conveniência, falha em silêncio.
            Log.i(TAG, "Não foi possível verificar atualizações", e)
            null
        }
    }
}

/** A release de maior versão, desde que seja maior que [currentVersionName] e tenha um APK. */
internal fun pickUpdate(releases: List<GitHubRelease>, currentVersionName: String): AppUpdate? {
    val current = parseVersion(currentVersionName) ?: return null
    return releases
        .filterNot { it.draft }
        .mapNotNull { release ->
            val version = parseVersion(release.tagName) ?: return@mapNotNull null
            val apk = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
                ?: return@mapNotNull null
            Triple(version, release, apk)
        }
        .filter { (version) -> compareVersions(version, current) > 0 }
        .maxWithOrNull { a, b -> compareVersions(a.first, b.first) }
        ?.let { (version, _, apk) -> AppUpdate(versionName = version.joinToString("."), downloadUrl = apk.downloadUrl) }
}

/**
 * "v1.2-teste" → [1, 2]. A tag da release precisa começar com o versionName do app
 * (ver app/build.gradle.kts); o sufixo depois de "-" é ignorado.
 */
internal fun parseVersion(text: String): List<Int>? {
    val numbers = text.trim().removePrefix("v").removePrefix("V").substringBefore('-').substringBefore('+')
        .split('.')
        .map { it.toIntOrNull() ?: return null }
    return numbers.takeIf { it.isNotEmpty() }
}

/** Compara número a número; partes faltando contam como 0 ("1.0" == "1.0.0"). */
internal fun compareVersions(a: List<Int>, b: List<Int>): Int {
    for (i in 0 until maxOf(a.size, b.size)) {
        val diff = a.getOrElse(i) { 0 }.compareTo(b.getOrElse(i) { 0 })
        if (diff != 0) return diff
    }
    return 0
}
