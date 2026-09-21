package com.bioscanner.di

import android.graphics.Bitmap
import com.bioscanner.domain.classifier.ImageClassifier
import com.bioscanner.domain.model.ClassificationCandidate
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

const val TEST_CLASSIFIER_LABEL = "test_species"
const val TEST_CLASSIFIER_CONFIDENCE = 0.9f

/**
 * Substitui [ClassifierModule] nos testes instrumentados por um classificador
 * determinístico e fixo — evita depender do comportamento pseudoaleatório de
 * MockImageClassifier (já coberto por MockImageClassifierTest) para manter o
 * teste de UI estável.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [ClassifierModule::class])
object TestClassifierModule {

    @Provides
    @Singleton
    fun provideImageClassifier(): ImageClassifier = object : ImageClassifier {
        override suspend fun classify(bitmap: Bitmap): List<ClassificationCandidate> =
            listOf(ClassificationCandidate(TEST_CLASSIFIER_LABEL, TEST_CLASSIFIER_CONFIDENCE))
    }
}
