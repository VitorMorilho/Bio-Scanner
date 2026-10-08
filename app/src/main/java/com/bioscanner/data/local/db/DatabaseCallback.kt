package com.bioscanner.data.local.db

import android.content.Context
import androidx.core.content.edit
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bioscanner.data.local.seed.SpeciesSeedLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider

private const val PREFS_NAME = "seed"
private const val KEY_SEED_VERSION = "seedVersion"

/**
 * Todo o conteúdo do banco vem de assets/seed/species_seed.json (não há dado
 * do usuário). A cada abertura compara a versão do seed embarcado com a última
 * aplicada e, se mudou, substitui tudo — assim editar o CSV, rodar o script e
 * reinstalar o app basta, sem precisar subir a versão do Room.
 */
class DatabaseCallback(
    private val context: Context,
    private val scope: CoroutineScope,
    private val databaseProvider: Provider<BioScannerDatabase>,
) : RoomDatabase.Callback() {

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        scope.launch(Dispatchers.IO) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val seed = SpeciesSeedLoader(context).load()
            val dao = databaseProvider.get().speciesDao()
            // count() == 0 cobre o banco recriado por migração destrutiva, quando
            // a versão salva nas prefs ainda é a antiga.
            if (prefs.getString(KEY_SEED_VERSION, null) != seed.version || dao.count() == 0) {
                dao.replaceAll(seed.species, seed.regions)
                prefs.edit { putString(KEY_SEED_VERSION, seed.version) }
            }
        }
    }
}
