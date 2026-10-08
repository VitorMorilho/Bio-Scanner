package com.bioscanner.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [SpeciesEntity::class, SpeciesRegionEntity::class, ObservationEntity::class],
    version = 3,
    exportSchema = true,
)
abstract class BioScannerDatabase : RoomDatabase() {
    abstract fun speciesDao(): SpeciesDao
    abstract fun observationDao(): ObservationDao
}

/**
 * v3 cria a tabela de registros do usuário. A partir daqui toda mudança de
 * schema precisa de uma Migration como esta — os registros não podem se perder.
 * O SQL deve bater com app/schemas/.../3.json (o Room valida ao abrir o banco).
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `observation` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `photoPath` TEXT NOT NULL, " +
                "`takenAt` INTEGER NOT NULL, `takenAtOrigin` TEXT NOT NULL, `latitude` REAL, " +
                "`longitude` REAL, `locationOrigin` TEXT, `status` TEXT NOT NULL, " +
                "`speciesId` TEXT, `confidence` REAL, `createdAt` INTEGER NOT NULL)",
        )
    }
}
