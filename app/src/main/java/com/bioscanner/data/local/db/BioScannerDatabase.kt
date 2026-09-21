package com.bioscanner.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SpeciesEntity::class], version = 1, exportSchema = true)
abstract class BioScannerDatabase : RoomDatabase() {
    abstract fun speciesDao(): SpeciesDao
}
