package com.bioscanner

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.bioscanner.data.local.db.ObservationDao
import com.bioscanner.data.local.db.ObservationEntity
import com.bioscanner.data.local.db.SpeciesDao
import com.bioscanner.data.local.db.SpeciesEntity
import com.bioscanner.di.TEST_CLASSIFIER_LABEL
import com.bioscanner.presentation.home.HomeScreen
import com.bioscanner.presentation.common.BottomNavTab
import com.bioscanner.presentation.navigation.BioScannerNavGraph
import com.bioscanner.presentation.navigation.Routes
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.java.KoinJavaComponent.get

class MainFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var speciesDao: SpeciesDao
    private lateinit var observationDao: ObservationDao

    @Before
    fun setUp() {
        ApplicationProvider.getApplicationContext<TestBioScannerApplication>().resetKoin()
        speciesDao = get(SpeciesDao::class.java)
        observationDao = get(ObservationDao::class.java)

        runBlocking {
            speciesDao.insertAll(
                listOf(
                    SpeciesEntity(
                        id = "test_species",
                        scientificName = "Testus speciesus",
                        popularName = "Espécie de Teste",
                        family = "Familia Teste",
                        category = "snake",
                        venomous = true,
                        riskLevel = "HIGH",
                        description = "desc",
                        geographicDistribution = "dist",
                        firstAidProcedure = "procedure",
                        referenceImagePath = null,
                        classifierLabel = TEST_CLASSIFIER_LABEL,
                    ),
                ),
            )
        }
    }

    private fun insertObservation(): Long = runBlocking {
        observationDao.insert(
            ObservationEntity(
                photoPath = "/nao/existe.jpg",
                takenAt = 1_700_000_000_000,
                takenAtOrigin = "DEVICE",
                latitude = -15.6,
                longitude = -56.1,
                locationOrigin = "DEVICE",
                status = "SAVED",
                speciesId = null,
                confidence = null,
                createdAt = 1_700_000_000_000,
            ),
        )
    }

    @Test
    fun observationDetailShowsStateFromCoordinates() {
        val id = insertObservation()
        composeRule.setContent {
            BioScannerNavGraph(startDestination = Routes.observation(id))
        }

        // -15.6, -56.1 fica em Cuiabá: o nome do estado vem dos contornos offline.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Mato Grosso", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("posição do aparelho no registro").assertExists()
    }

    @Test
    fun deletingObservationAsksForConfirmationAndRemovesIt() {
        val id = insertObservation()
        composeRule.setContent {
            BioScannerNavGraph(startDestination = Routes.observation(id))
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Excluir registro").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Excluir registro").performClick()
        composeRule.onNodeWithText("Excluir").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            runBlocking { observationDao.findById(id) == null }
        }
    }

    @Test
    fun recordsScreenListsSavedObservation() {
        insertObservation()
        composeRule.setContent {
            BioScannerNavGraph(startDestination = Routes.RECORDS)
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Mato Grosso").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Entrar ou criar conta").assertExists()
    }

    @Test
    fun homeScreenShowsDisclaimerAndRegisterEntryPoint() {
        composeRule.setContent {
            HomeScreen(onCaptureClick = {}, onTabClick = { _: BottomNavTab -> })
        }

        composeRule.onNodeWithText("Toque para registrar").assertExists()
        composeRule
            .onNodeWithText("Esta identificação é apenas uma sugestão automática", substring = true)
            .assertExists()
    }
}
