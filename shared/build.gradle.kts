plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
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
    // Framework estático "Shared" consumido pelo app Xcode (ios/), gerado pela task
    // embedAndSignAppleFrameworkForXcode na fase de build do Xcode.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

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

            // Telas (Compose Multiplatform).
            api(libs.cmp.runtime)
            api(libs.cmp.foundation)
            api(libs.cmp.ui)
            implementation(libs.cmp.ui.backhandler)
            api(libs.cmp.material3)
            implementation(libs.cmp.material.icons.extended)
            implementation(libs.cmp.components.resources)
            api(libs.jb.lifecycle.viewmodel.compose)
            implementation(libs.jb.lifecycle.runtime.compose)
            implementation(libs.jb.navigation.compose)
            implementation(libs.koin.compose.viewmodel)
        }
        // Motor HTTP do Ktor usado pelo supabase-kt em cada plataforma.
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            // Peças Android das telas: câmera, permissões/galeria e login com Google.
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.googleid)
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

compose.resources {
    // O padrão deriva do nome do projeto ("Bio Scanner", com espaço), que o D8 recusa em nome de classe.
    packageOfResClass = "com.bioscanner.resources"
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}
