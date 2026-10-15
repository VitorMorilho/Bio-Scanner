plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

/**
 * Código compartilhado entre Android e iOS: modelos, casos de uso, banco
 * (Room KMP) e leitura dos dados. Os alvos iOS compilam no Linux (ver
 * kotlin.native.enableKlibsCrossCompilation em gradle.properties); gerar o
 * framework para o app iOS exige macOS.
 */
kotlin {
    android {
        namespace = "com.bioscanner.shared"
        compileSdk = 36
        minSdk = 24
        withHostTestBuilder {}
    }
    iosArm64()
    iosSimulatorArm64()

    compilerOptions {
        // expect object do Room (BioScannerDatabaseConstructor).
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            api(libs.kotlinx.coroutines.core)
            api(libs.koin.core)
            implementation(libs.kotlinx.serialization.json)
            // Contas (login opcional). api: o app Android monta o cliente e injeta os tipos.
            api(libs.supabase.auth)
            api(libs.supabase.functions)
        }
        // Motor HTTP do Ktor usado pelo supabase-kt em cada plataforma.
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}
