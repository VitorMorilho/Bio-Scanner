package com.bioscanner.di

import android.graphics.Bitmap
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.bioscanner.data.local.db.BioScannerDatabase
import com.bioscanner.domain.auth.AuthRepository
import com.bioscanner.domain.auth.AuthUser
import com.bioscanner.domain.auth.SignUpResult
import com.bioscanner.domain.classifier.ImageClassifier
import com.bioscanner.domain.model.ClassificationCandidate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

const val TEST_CLASSIFIER_LABEL = "test_species"
const val TEST_CLASSIFIER_CONFIDENCE = 0.9f

/**
 * Substituições para os testes instrumentados, carregadas depois de [appModules] (a última
 * definição de um tipo vence):
 * - banco Room em memória, sem o callback assíncrono de seed: os testes semeiam o que precisam,
 *   de forma síncrona, para evitar flakiness;
 * - sem rede nem Supabase: o usuário fica sempre deslogado (o fluxo de conta é coberto pelos
 *   testes unitários do AuthViewModel);
 * - classificador fixo, sem depender do comportamento pseudoaleatório do MockImageClassifier.
 */
val testOverridesModule = module {
    single<BioScannerDatabase> {
        // Mesmo driver do app (SQLite embutido, ver BioScannerDatabase.configure).
        Room.inMemoryDatabaseBuilder<BioScannerDatabase>(androidContext())
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    single<AuthRepository> {
        object : AuthRepository {
            override val currentUser: Flow<AuthUser?> = flowOf(null)
            override suspend fun signUp(name: String, email: String, password: String) = SignUpResult.NEEDS_EMAIL_CONFIRMATION
            override suspend fun confirmSignUp(email: String, code: String) = Unit
            override suspend fun resendSignUpCode(email: String) = Unit
            override suspend fun signIn(email: String, password: String) = Unit
            override suspend fun signInWithGoogle(idToken: String, rawNonce: String) = Unit
            override suspend fun sendPasswordResetCode(email: String) = Unit
            override suspend fun resetPassword(email: String, code: String, newPassword: String) = Unit
            override suspend fun signOut() = Unit
            override suspend fun deleteAccount() = Unit
        }
    }

    single<ImageClassifier> {
        object : ImageClassifier {
            override suspend fun classify(bitmap: Bitmap): List<ClassificationCandidate> =
                listOf(ClassificationCandidate(TEST_CLASSIFIER_LABEL, TEST_CLASSIFIER_CONFIDENCE))
        }
    }
}
