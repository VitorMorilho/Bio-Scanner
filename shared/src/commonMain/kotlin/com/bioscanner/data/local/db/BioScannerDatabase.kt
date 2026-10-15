package com.bioscanner.data.local.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

const val DATABASE_NAME = "bioscanner.db"

@Database(
    entities = [SpeciesEntity::class, SpeciesRegionEntity::class, ObservationEntity::class],
    version = 3,
    exportSchema = true,
)
@ConstructedBy(BioScannerDatabaseConstructor::class)
abstract class BioScannerDatabase : RoomDatabase() {
    abstract fun speciesDao(): SpeciesDao
    abstract fun observationDao(): ObservationDao
}

// O Room gera o `actual` de cada plataforma (KSP); não escrever à mão.
@Suppress("KotlinNoActualForExpect")
expect object BioScannerDatabaseConstructor : RoomDatabaseConstructor<BioScannerDatabase> {
    override fun initialize(): BioScannerDatabase
}

/**
 * v3 cria a tabela de registros do usuário. A partir daqui toda mudança de
 * schema precisa de uma Migration como esta — os registros não podem se perder.
 * O SQL deve bater com shared/schemas/.../3.json (o Room valida ao abrir o banco).
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `observation` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `photoPath` TEXT NOT NULL, " +
                "`takenAt` INTEGER NOT NULL, `takenAtOrigin` TEXT NOT NULL, `latitude` REAL, " +
                "`longitude` REAL, `locationOrigin` TEXT, `status` TEXT NOT NULL, " +
                "`speciesId` TEXT, `confidence` REAL, `createdAt` INTEGER NOT NULL)",
        )
    }
}

/**
 * Configuração comum às duas plataformas; cada uma só cria o Builder com o
 * caminho do arquivo (ver androidMain/iosMain).
 *
 * SQLite embutido (BundledSQLiteDriver) nas duas: mesma versão do SQLite no
 * Android e no iOS. O arquivo é o mesmo formato do SQLite do sistema, então o
 * banco já existente no Android abre normalmente.
 */
fun RoomDatabase.Builder<BioScannerDatabase>.configure(
    seedingCallback: RoomDatabase.Callback,
): BioScannerDatabase = this
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .addMigrations(MIGRATION_2_3)
    // Só a v1 pode ser recriada do zero: ela tinha apenas o seed dos
    // assets. Da v2 em diante há registros do usuário → só Migration.
    .fallbackToDestructiveMigrationFrom(dropAllTables = true, 1)
    .addCallback(seedingCallback)
    .build()
