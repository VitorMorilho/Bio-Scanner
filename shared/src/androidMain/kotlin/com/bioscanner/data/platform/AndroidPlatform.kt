package com.bioscanner.data.platform

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bioscanner.data.local.db.BioScannerDatabase
import com.bioscanner.data.local.db.DATABASE_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Mesmo nome e local de antes da migração para KMP: os dados já salvos continuam. */
fun androidDatabaseBuilder(context: Context): RoomDatabase.Builder<BioScannerDatabase> {
    val appContext = context.applicationContext
    return Room.databaseBuilder<BioScannerDatabase>(
        context = appContext,
        name = appContext.getDatabasePath(DATABASE_NAME).absolutePath,
    )
}

class AssetsResourceReader(private val context: Context) : ResourceReader {
    override suspend fun readText(path: String): String = withContext(Dispatchers.IO) {
        context.assets.open(path).bufferedReader().use { it.readText() }
    }
}

/** Mesmas prefs/chave usadas antes ("seed"/"seedVersion"): não força recarga ao atualizar. */
class SharedPrefsSeedVersionStore(context: Context) : SeedVersionStore {
    private val prefs = context.getSharedPreferences("seed", Context.MODE_PRIVATE)
    override fun get(): String? = prefs.getString(KEY, null)
    override fun set(version: String) {
        prefs.edit().putString(KEY, version).apply()
    }

    private companion object {
        const val KEY = "seedVersion"
    }
}
