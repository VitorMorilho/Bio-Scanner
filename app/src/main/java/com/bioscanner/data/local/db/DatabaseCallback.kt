package com.bioscanner.data.local.db

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bioscanner.data.local.seed.SpeciesSeedLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider

class DatabaseCallback(
    private val context: Context,
    private val scope: CoroutineScope,
    private val databaseProvider: Provider<BioScannerDatabase>,
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        scope.launch(Dispatchers.IO) {
            val dao = databaseProvider.get().speciesDao()
            if (dao.count() == 0) {
                val seed = SpeciesSeedLoader(context).loadSeedEntities()
                dao.insertAll(seed)
            }
        }
    }
}
