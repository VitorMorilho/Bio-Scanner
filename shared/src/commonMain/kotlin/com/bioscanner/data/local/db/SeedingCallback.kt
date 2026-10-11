package com.bioscanner.data.local.db

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import com.bioscanner.data.local.seed.SpeciesSeedLoader
import com.bioscanner.data.platform.SeedVersionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * As tabelas species/species_region vêm inteiras de assets/seed/species_seed.json.
 * A cada abertura compara a versão do seed embarcado com a última aplicada e,
 * se mudou, substitui tudo — assim editar o CSV, rodar o script e reinstalar o
 * app basta, sem precisar subir a versão do Room. Nunca toca em observation.
 *
 * [database] é preguiçoso porque o callback é criado antes do banco existir.
 */
class SeedingCallback(
    private val seedLoader: SpeciesSeedLoader,
    private val versionStore: SeedVersionStore,
    private val scope: CoroutineScope,
    private val database: () -> BioScannerDatabase,
) : RoomDatabase.Callback() {

    override fun onOpen(connection: SQLiteConnection) {
        scope.launch {
            val seed = seedLoader.load()
            val dao = database().speciesDao()
            // count() == 0 cobre o banco recriado por migração destrutiva, quando
            // a versão salva ainda é a antiga.
            if (versionStore.get() != seed.version || dao.count() == 0) {
                dao.replaceAll(seed.species, seed.regions)
                versionStore.set(seed.version)
            }
        }
    }
}
