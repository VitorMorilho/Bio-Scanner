import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

// Credenciais do Supabase ficam no local.properties (fora do git). A anon key é pública por
// natureza (protegida pelas regras RLS); a service_role NUNCA entra aqui.
val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

android {
    namespace = "com.bioscanner"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.bioscanner"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "com.bioscanner.CustomTestRunner"

        buildConfigField("String", "SUPABASE_URL", "\"${localProperties.getProperty("supabase.url", "")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProperties.getProperty("supabase.anonKey", "")}\"")
        // Client ID do OAuth Client tipo *Web* (o mesmo informado no Supabase). Vazio = sem botão do Google.
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${localProperties.getProperty("google.webClientId", "")}\"")
        // true só quando o Supabase tiver SMTP próprio e templates com código; sem isso a
        // recuperação de senha por email não chega ao usuário, então a opção fica escondida.
        buildConfigField("boolean", "EMAIL_CODES_ENABLED", localProperties.getProperty("auth.emailCodes", "false"))
    }

    buildTypes {
        // Aviso de atualização: o app procura APKs novos nas Releases deste repositório. A tag de
        // cada release começa com o versionName (ex.: v1.1-teste), e versionCode/versionName
        // precisam subir a cada versão publicada. Só no release: em debug o aviso atrapalharia.
        debug {
            buildConfigField("String", "UPDATE_REPOSITORY", "\"\"")
        }
        release {
            buildConfigField("String", "UPDATE_REPOSITORY", "\"VitorMorilho/Bio-Scanner\"")
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    // Schemas exportados do Room (gerados pelo módulo shared) ficam disponíveis
    // para o MigrationTestHelper dos testes instrumentados.
    sourceSets {
        getByName("androidTest").assets.directories.add("${rootDir}/shared/schemas")
    }
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // tensorflow-lite-support is deliberately NOT used: its AAR (and LiteRT's
    // equivalent replacement) has an upstream packaging bug where
    // tensorflow-lite-support / tensorflow-lite-support-api both declare the
    // same AndroidManifest namespace, which AGP 9's stricter uniqueness check
    // rejects. Preprocessing will be done manually via ByteBuffer instead.
    implementation(libs.tensorflow.lite)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // supabase-kt vem do :shared (api). OkHttp direto para o GitHubUpdateChecker.
    implementation(libs.ktor.client.okhttp)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.core)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.sqlite.bundled)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    kspAndroidTest(libs.hilt.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}