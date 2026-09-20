package com.bioscanner

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.bioscanner.data.local.db.SpeciesDao
import com.bioscanner.data.local.db.SpeciesEntity
import com.bioscanner.di.TEST_CLASSIFIER_LABEL
import com.bioscanner.presentation.home.HomeScreen
import com.bioscanner.presentation.navigation.BioScannerNavGraph
import com.bioscanner.presentation.navigation.Routes
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltAndroidTest
class MainFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var speciesDao: SpeciesDao

    private lateinit var testImageUri: Uri

    @Before
    fun setUp() {
        hiltRule.inject()

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

        testImageUri = createTestImageUri()
    }

    private fun createTestImageUri(): Uri {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val file = File(context.cacheDir, "main_flow_test.jpg")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        return Uri.fromFile(file)
    }

    @Test
    fun resultScreenShowsDisclaimerAndMatchedSpecies() {
        val encodedUri = Uri.encode(testImageUri.toString())
        composeRule.setContent {
            BioScannerNavGraph(startDestination = Routes.result(encodedUri))
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Espécie de Teste").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Espécie de Teste").assertExists()
        composeRule
            .onNodeWithText(
                "Esta identificação é apenas uma sugestão automática e não substitui " +
                    "avaliação de um profissional ou atendimento médico. Em caso de picada, " +
                    "procure ajuda médica imediatamente.",
                substring = true,
            )
            .assertExists()
    }

    @Test
    fun homeScreenShowsDisclaimerAndCaptureEntryPoint() {
        composeRule.setContent {
            HomeScreen(onCaptureClick = {})
        }

        composeRule.onNodeWithText("Fotografar ou escolher da galeria").assertExists()
        composeRule
            .onNodeWithText(
                "Esta identificação é apenas uma sugestão automática e não substitui " +
                    "avaliação de um profissional ou atendimento médico. Em caso de picada, " +
                    "procure ajuda médica imediatamente.",
                substring = true,
            )
            .assertExists()
    }
}
