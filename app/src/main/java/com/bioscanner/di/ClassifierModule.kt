package com.bioscanner.di

import com.bioscanner.data.classifier.MockImageClassifier
import com.bioscanner.domain.classifier.ImageClassifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Ponto de troca: quando o modelo .tflite real e o labels.txt estiverem
 * embarcados, trocar `MockImageClassifier` por `TfLiteImageClassifier` aqui
 * — nenhum outro arquivo em domain/ ou presentation/ precisa mudar.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ClassifierModule {

    @Binds
    @Singleton
    abstract fun bindImageClassifier(impl: MockImageClassifier): ImageClassifier
}
