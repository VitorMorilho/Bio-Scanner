package com.bioscanner.data.platform

import androidx.room.Room
import androidx.room.RoomDatabase
import com.bioscanner.data.local.db.BioScannerDatabase
import com.bioscanner.data.local.db.DATABASE_NAME
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringWithContentsOfFile

/** Banco em Documents/ do app (incluído no backup do iCloud, como os registros devem ser). */
@OptIn(ExperimentalForeignApi::class)
fun iosDatabaseBuilder(): RoomDatabase.Builder<BioScannerDatabase> {
    val documents: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    val path = requireNotNull(documents?.path) { "Pasta Documents indisponível" } + "/$DATABASE_NAME"
    return Room.databaseBuilder<BioScannerDatabase>(name = path)
}

/**
 * Pasta do bundle com uma cópia de app/src/main/assets do Android (referência de pasta no
 * projeto Xcode, ver ios/project.yml), mantendo as subpastas (ex.: "assets/seed/species_seed.json").
 */
const val BUNDLE_ASSETS_DIR = "assets"

/** Lê os mesmos arquivos que o Android lê de assets/, a partir do bundle do app. */
@OptIn(ExperimentalForeignApi::class)
class BundleResourceReader : ResourceReader {
    override suspend fun readText(path: String): String {
        val fullPath = requireNotNull(NSBundle.mainBundle.resourcePath) { "Bundle sem resourcePath" } + "/$BUNDLE_ASSETS_DIR/$path"
        return requireNotNull(
            NSString.stringWithContentsOfFile(fullPath, encoding = NSUTF8StringEncoding, error = null),
        ) { "Recurso não encontrado no bundle: $path" }
    }
}

class UserDefaultsSeedVersionStore : SeedVersionStore {
    private val defaults = NSUserDefaults.standardUserDefaults
    override fun get(): String? = defaults.stringForKey(KEY)
    override fun set(version: String) = defaults.setObject(version, forKey = KEY)

    private companion object {
        const val KEY = "seedVersion"
    }
}
