package com.bioscanner.data.local.db

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "migration-test"

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        BioScannerDatabase::class.java,
    )

    @Test
    fun migrate2To3CreatesObservationTableAndKeepsSpecies() {
        helper.createDatabase(TEST_DB, 2).use { db ->
            db.execSQL(
                "INSERT INTO species (id, scientificName, popularName, family, category, venomous, riskLevel, " +
                    "description, geographicDistribution, firstAidProcedure, referenceImagePath, classifierLabel, " +
                    "otherNames) VALUES ('sp1', 'Sci', 'Pop', 'Fam', 'snake', 1, 'HIGH', NULL, NULL, NULL, NULL, 'sp1', '')",
            )
        }

        // Valida o schema resultante contra app/schemas/.../3.json.
        helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_2_3).use { db ->
            db.query("SELECT COUNT(*) FROM species").use { c ->
                c.moveToFirst()
                assertEquals(1, c.getInt(0))
            }
            db.execSQL(
                "INSERT INTO observation (photoPath, takenAt, takenAtOrigin, status, createdAt) " +
                    "VALUES ('/x.jpg', 0, 'DEVICE', 'PENDING', 0)",
            )
        }
    }
}
