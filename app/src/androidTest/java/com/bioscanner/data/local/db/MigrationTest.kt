package com.bioscanner.data.local.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "migration-test"

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    // Mesmo driver do app (Room KMP): a migração usa a API de SQLiteConnection.
    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = instrumentation,
        file = instrumentation.targetContext.getDatabasePath(TEST_DB),
        driver = BundledSQLiteDriver(),
        databaseClass = BioScannerDatabase::class,
    )

    @Test
    fun migrate2To3CreatesObservationTableAndKeepsSpecies() {
        helper.createDatabase(2).apply {
            execSQL(
                "INSERT INTO species (id, scientificName, popularName, family, category, venomous, riskLevel, " +
                    "description, geographicDistribution, firstAidProcedure, referenceImagePath, classifierLabel, " +
                    "otherNames) VALUES ('sp1', 'Sci', 'Pop', 'Fam', 'snake', 1, 'HIGH', NULL, NULL, NULL, NULL, 'sp1', '')",
            )
            close()
        }

        // Valida o schema resultante contra shared/schemas/.../3.json.
        val db = helper.runMigrationsAndValidate(3, listOf(MIGRATION_2_3))
        db.prepare("SELECT COUNT(*) FROM species").use { statement ->
            statement.step()
            assertEquals(1L, statement.getLong(0))
        }
        db.execSQL(
            "INSERT INTO observation (photoPath, takenAt, takenAtOrigin, status, createdAt) " +
                "VALUES ('/x.jpg', 0, 'DEVICE', 'SAVED', 0)",
        )
        db.close()
    }

    @Test
    fun migrate3To4AddsColorPatternsAndKeepsObservations() {
        helper.createDatabase(3).apply {
            execSQL(
                "INSERT INTO species (id, scientificName, popularName, family, category, venomous, riskLevel, " +
                    "description, geographicDistribution, firstAidProcedure, referenceImagePath, classifierLabel, " +
                    "otherNames) VALUES ('sp1', 'Sci', 'Pop', 'Fam', 'snake', 1, 'HIGH', NULL, NULL, NULL, NULL, 'sp1', '')",
            )
            execSQL(
                "INSERT INTO observation (photoPath, takenAt, takenAtOrigin, status, createdAt) " +
                    "VALUES ('/x.jpg', 0, 'DEVICE', 'SAVED', 0)",
            )
            close()
        }

        // Valida o schema resultante contra shared/schemas/.../4.json.
        val db = helper.runMigrationsAndValidate(4, listOf(MIGRATION_3_4))
        db.prepare("SELECT colorPatterns FROM species WHERE id = 'sp1'").use { statement ->
            statement.step()
            assertEquals("", statement.getText(0))
        }
        db.prepare("SELECT COUNT(*) FROM observation").use { statement ->
            statement.step()
            assertEquals(1L, statement.getLong(0))
        }
        db.close()
    }
}
