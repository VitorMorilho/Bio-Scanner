package com.bioscanner.data.local.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Dispatchers
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
        db = Room.inMemoryDatabaseBuilder<BioScannerDatabase>(context)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
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
    fun getSpeciesInRegionJoinsAndOrdersByOccurrences() = runBlocking {
        dao.replaceAll(
            species = listOf(entity("sp1"), entity("sp2"), entity("sp3")),
            regions = listOf(
                SpeciesRegionEntity("sp1", "MT", 5),
                SpeciesRegionEntity("sp2", "MT", 40),
                SpeciesRegionEntity("sp3", "PR", 7),
            ),
        )

        val inMt = dao.getSpeciesInRegion("MT").first()

        assertEquals(listOf("sp2", "sp1"), inMt.map { it.species.id })
        assertEquals(40, inMt.first().occurrences)
        assertEquals(listOf("PR"), dao.getRegionIdsForSpecies("sp3").first())
    }

    @Test
    fun replaceAllDropsPreviousContent() = runBlocking {
        dao.replaceAll(listOf(entity("old")), listOf(SpeciesRegionEntity("old", "MT", 1)))
        dao.replaceAll(listOf(entity("new")), emptyList())

        assertEquals(1, dao.count())
        assertEquals(emptyList<SpeciesInRegion>(), dao.getSpeciesInRegion("MT").first())
    }

    @Test
    fun countReflectsNumberOfSeededRows() = runBlocking {
        assertEquals(0, dao.count())

        dao.insertAll(listOf(entity("sp1"), entity("sp2"), entity("sp3")))

        assertEquals(3, dao.count())
    }
}
