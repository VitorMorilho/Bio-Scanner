package com.bioscanner.data.local.db

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpeciesDaoTest {

    private lateinit var db: BioScannerDatabase
    private lateinit var dao: SpeciesDao

    private fun entity(id: String, label: String = id) = SpeciesEntity(
        id = id,
        scientificName = "Sci $id",
        popularName = "Pop $id",
        family = "Fam",
        category = "snake",
        venomous = true,
        riskLevel = "HIGH",
        description = "desc",
        geographicDistribution = "dist",
        firstAidProcedure = "procedure",
        referenceImagePath = null,
        classifierLabel = label,
    )

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, BioScannerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.speciesDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAllAndGetAllSpeciesEmitsInsertedRows() = runBlocking {
        dao.insertAll(listOf(entity("sp1"), entity("sp2")))

        val all = dao.getAllSpecies().first()

        assertEquals(2, all.size)
    }

    @Test
    fun findByClassifierLabelReturnsMatchingEntity() = runBlocking {
        dao.insertAll(listOf(entity("sp1", label = "label_sp1")))

        val found = dao.findByClassifierLabel("label_sp1")
        val notFound = dao.findByClassifierLabel("nonexistent")

        assertEquals("sp1", found?.id)
        assertNull(notFound)
    }

    @Test
    fun countReflectsNumberOfSeededRows() = runBlocking {
        assertEquals(0, dao.count())

        dao.insertAll(listOf(entity("sp1"), entity("sp2"), entity("sp3")))

        assertEquals(3, dao.count())
    }
}
