package com.bioscanner.domain.update

/** Versão mais nova publicada, com o link direto do APK. */
data class AppUpdate(val versionName: String, val downloadUrl: String)

interface UpdateChecker {
    /** A versão mais nova que [currentVersionName], ou null se não houver (ou sem internet). */
    suspend fun findUpdate(currentVersionName: String): AppUpdate?
}
