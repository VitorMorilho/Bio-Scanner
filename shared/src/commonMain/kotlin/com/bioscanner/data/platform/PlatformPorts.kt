package com.bioscanner.data.platform

/**
 * Lê arquivos de dados empacotados no app (seed, contornos dos estados).
 * Android: assets/. iOS: recursos do bundle do app.
 */
interface ResourceReader {
    /** [path] relativo à raiz dos recursos, ex.: "seed/species_seed.json". */
    suspend fun readText(path: String): String
}

/**
 * Guarda a versão do seed já aplicada ao banco (ver SeedingCallback).
 * Android: SharedPreferences. iOS: NSUserDefaults.
 */
interface SeedVersionStore {
    fun get(): String?
    fun set(version: String)
}
