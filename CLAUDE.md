# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

BioScanner is an Android app (Kotlin + Jetpack Compose) that identifies animal/plant species
from a photo. The user takes/picks a photo, an on-device TensorFlow Lite model classifies the
species, and the app looks up the species' full description in a local Room database (name,
scientific name, family, venomous flag, risk level, description, geographic distribution,
first-aid guidance, reference image).

**Scope note:** the first release focuses only on snakes, but the product is meant to expand to
other categories later (other reptiles, insects, plants, etc.). Prefer generic naming and
modeling now so this isn't a rewrite later:
- The core Room entity should be called `Species` (not `Snake`), with an optional `category`
  field (e.g. "snake") rather than snake-only fields baked into the schema.
- The classifier abstraction should be thought of as "an image classification model", not
  snake-specific, even though the first bundled `.tflite` model only knows snakes.
- Don't over-engineer for future categories the user hasn't asked for yet — just avoid names/
  structures that would lock the app into snakes only.

iOS is being added via **Kotlin Multiplatform**, in phases (the user has no Mac — iOS code can
only be compiled, not run, on this Linux machine):
1. **Done:** `shared/` KMP module with domain, data, Room and seed loading; the Android app uses it.
2. In progress. **Done:** login (Supabase) moved to `shared`; DI is Koin (Hilt removed); all
   screens, ViewModels and theme are Compose Multiplatform in `shared/commonMain`.
   Next: iOS implementations of the platform pieces (marked `TODO(iOS)`: camera/gallery screen,
   location permission, Google sign-in; plus `PhotoStorage`/`CaptureMetadataProvider` and an iOS
   Koin platform module), Sign in with Apple.
3. Then: Xcode project in `ios/` + GitHub Actions macOS runner to build it (repo is public).

## Project status

Main flow: Home → Capture → Register (photo + date/time + location, "Salvar nos meus
registros") → Meus registros. A photo becomes an *observation* (registro) saved to the user's
records; there is **no species identification/analysis in the flow anymore** (the user dropped
both instant identification and "send for analysis"). The classifier stack (`ImageClassifier`,
`MockImageClassifier`, `ClassifyAndLookupSpeciesUseCase`, `ConfidenceBadge`) is kept but
currently unused by any screen, pending the real model. Bottom nav: Registrar · Glossário
(species search + map by state) · Meus registros (user card with optional login + the list of
observations). No real
dataset or trained `.tflite` model exists yet. Species content is real: ~380 Brazilian snake
species from GBIF, one open-license photo each (iNaturalist), and hand-curated text for the
species filled in `tools/data/especies.csv` (see "Species data pipeline").

Not yet a git repo with commits (a plain `git init` was run, but no author identity is
configured — set `user.name`/`user.email` locally in this repo before committing).

## Commands

This is a Gradle project; use the wrapper (`./gradlew`) from the repo root. Android Studio's JBR
works as JDK: `export JAVA_HOME=/snap/android-studio/current/jbr`.

- Shared module unit tests (commonTest, run on the JVM): `./gradlew :shared:testAndroidHostTest`
- Compile shared for iOS on Linux (klibs only; needs no Mac): `./gradlew :shared:compileKotlinIosArm64 :shared:compileKotlinIosSimulatorArm64 :shared:compileTestKotlinIosSimulatorArm64`

- Build debug APK: `./gradlew assembleDebug`
- Run local (JVM) unit tests: `./gradlew test`
- Run a single local unit test class: `./gradlew test --tests "com.bioscanner.domain.usecase.ClassifyAndLookupSpeciesUseCaseTest"`
- Compile instrumented test sources without a device: `./gradlew assembleDebugAndroidTest`
- Run instrumented tests (needs a connected device/emulator): `./gradlew connectedAndroidTest`
- Lint: `./gradlew lint`
- Full check (lint + tests): `./gradlew check`

## Architecture

- Modules: `app` (Android app, applicationId/namespace `com.bioscanner`: UI, ViewModels, and Android
  implementations of platform ports) and `shared` (Kotlin Multiplatform library — android + iosArm64
  + iosSimulatorArm64 — with `domain/`, `data/` (Room KMP, seed/regions parsing, repositories) and
  `data/platform/` ports `ResourceReader`/`SeedVersionStore` with androidMain/iosMain impls,
  `data/auth/` (Supabase) and `di/sharedModule`).
  Packages kept the same names when moved, so `com.bioscanner.domain.*`/`data.*` live in `shared`.
  Code in `shared/commonMain` must be pure Kotlin: no `java.*`, no `android.*`, no `javax.inject`
  (classes are wired in Koin modules), no `System.currentTimeMillis` (use `kotlin.time.Clock`). Tests there use `kotlin.test`; backticked test names can't contain `,` or `()` on iOS.
- Kotlin is **2.3.21** (declared over AGP 9's bundled 2.2.10): kotlinx libraries 1.11 are published
  with Kotlin 2.3 and the 2.2 Kotlin/Native compiler can't read their klibs.
  `kotlin.native.enableKlibsCrossCompilation=true` (gradle.properties) lets iOS klibs compile on Linux.
- UI is **Compose Multiplatform** (no XML layouts): screens, ViewModels (multiplatform
  `androidx.lifecycle`, `SavedStateHandle` included) and navigation (`org.jetbrains.androidx.navigation`)
  live in `shared/commonMain/.../presentation/`; `app` only has `MainActivity` (theme + nav graph +
  `UpdatePrompt`) and Android data implementations. Versions are pinned as a set matching the app's
  Compose 1.9 (see `libs.versions.toml`). Platform-specific UI is `expect`/`actual` (files named
  `*.android.kt` / `*.ios.kt` to avoid JVM class clashes): `CaptureScreen` (CameraX + photo picker
  on Android), `rememberLocationPermissionState`, `rememberGoogleSignIn`, `formatDateTime`,
  `logWarning`/`logError`. Images go through the `ImageLoader` port (`AndroidImageLoader`:
  BitmapFactory + EXIF rotation; `IosImageLoader`: Skia). Platform config reaches screens as
  `AppConfig` (Koin). Android-only APIs (`android.*`, `R`, `BuildConfig`) can't be used in commonMain.
- Resources: fonts and the intro image are in `shared/src/commonMain/composeResources/`
  (`Res` class in package `com.bioscanner.resources`: the default package would come from the
  project name "Bio Scanner", whose space D8 rejects). Species data/photos stay in `app/src/main/assets/`
  (read via `ResourceReader`/`ImageLoader`; the iOS bundle will copy them).
- Theming lives in `shared/.../ui/theme/` (`Color.kt`, `Theme.kt`, `Type.kt`)
  — `BioScannerTheme` wraps all Compose content and should be reused for any new screens.
- Dependency versions are centralized in the Gradle version catalog at
  `gradle/libs.versions.toml`; add new dependencies there rather than hardcoding versions in
  `app/build.gradle.kts`.
- Build config: compileSdk 36, minSdk 24, targetSdk 36 (app: AGP 9's built-in Kotlin, not
  `org.jetbrains.kotlin.android`; shared: `com.android.kotlin.multiplatform.library`), AGP 9.1.1, Java 11
  source/target compatibility, KSP `2.3.12` (decoupled version, not Kotlin-prefixed — AGP 9's
  built-in Kotlin rejects the old `<kotlin>-<ksp>` versioning; only `shared` uses it, for Room),
  Koin `4.2.2` (needs Kotlin 2.3.20+).
- MVVM + Clean Architecture under `com.bioscanner`: `domain/` (model, repository interface,
  `ImageClassifier` interface, use cases — no Android/Room/DI imports except
  `android.graphics.Bitmap` in the classifier interface, kept for simplicity per the scope note
  below), `data/` (Room, seed loading, repository impl, classifiers), `presentation/`
  (navigation + one package per screen: `home`, `capture`, `result`, `detail`, plus `common` for
  shared composables like `DisclaimerBanner`), `di/` (Koin modules).
- Dependency injection: **Koin** (multiplatform; Hilt was Android-only). `shared/di/SharedModule.kt`
  (`sharedModule`) has everything common: app `CoroutineScope` for DB seeding, Room database + DAOs,
  repositories, use cases, Supabase client and `AuthRepository`. It expects each platform to provide
  `RoomDatabase.Builder<BioScannerDatabase>`, `ResourceReader`, `SeedVersionStore`, `PhotoStorage`,
  `CaptureMetadataProvider`, `SupabaseConfig` and `PlatformCredentialsCleaner`; on Android that's
  `androidPlatformModule` in `app/di/AppModules.kt`, next to `classifierModule` and `viewModelModule`
  (`viewModelOf`; screens use `koinViewModel()`, which also passes nav args via `SavedStateHandle`).
  `BioScannerApplication` calls `startKoin`. Classes have plain constructors (no `@Inject`).
  Instrumented tests: `CustomTestRunner` swaps in `TestBioScannerApplication` (app modules +
  `testOverridesModule`: in-memory DB, signed-out `AuthRepository`, fixed classifier) and tests call
  `resetKoin()` in `@Before` so every test gets fresh singletons, as Hilt did. Koin only fails at
  runtime on a missing definition, so after DI changes run `connectedDebugAndroidTest` and open
  every screen.
- Camera: CameraX (`presentation/capture/CameraPreviewView.kt`) writes captures straight to
  `MediaStore` (`Pictures/BioScanner`) via `ImageCapture.OutputFileOptions` — no `FileProvider`
  needed, since `MediaStore` already returns an app-usable `content://` URI and needs no extra
  storage permission on scoped-storage-era Android. Gallery uses
  `ActivityResultContracts.PickVisualMedia()` (modern photo picker). Camera permission is
  optional at runtime — if denied, gallery picking still works.
- Local database: Room KMP v3 (`shared/.../data/local/db/`, schemas in `shared/schemas/`), with
  `BundledSQLiteDriver` on both platforms; common config in `BioScannerDatabase.configure()`,
  per-platform builders `androidDatabaseBuilder`/`iosDatabaseBuilder`. Migrations use the
  `SQLiteConnection` API (not `SupportSQLiteDatabase`). Tables: flat `species` table (every field 1:1 with a
  species; curated fields are nullable) + `species_region` N:N join (species × UF, with GBIF
  occurrence counts) + `observation` (the user's records; its `status`/`speciesId`/`confidence` columns are leftovers
  from the dropped analysis flow, kept to avoid another migration — `status` is always `SAVED`). `classifierLabel` is a separate column
  from `id`, since a real model's label taxonomy may not match the DB's internal slugs 1:1.
  `species`/`species_region` are asset-derived: `SeedingCallback.onOpen` replaces them whenever
  the seed's `version` hash differs from the one in `SeedVersionStore` (SharedPreferences / NSUserDefaults) (it never touches
  `observation`). **`observation` is user data: every schema change needs a `Migration`** (see
  `MIGRATION_2_3` and `androidTest/.../MigrationTest`, which validates against
  `shared/schemas/`); destructive fallback is only allowed from v1. `observation.speciesId` is
  deliberately not a FK, since reseeding deletes and reinserts species.
- Observations (`domain/observation/` ports, `data/observation/` impls, bound in
  `androidPlatformModule`): `CaptureMetadataProvider` (camera photo → now + device location; gallery
  photo → EXIF date/location, falling back to device; platform `LocationManager`, no Play
  Services; location permission is optional), `PhotoStorage` (copies the photo into
  `filesDir/observations/`, downscaled to 1600 px and EXIF-rotated — picker URIs expire). The Android photo picker usually strips EXIF location, so
  gallery photos often get the device location instead (the UI says which origin each field has).
- ML / recognition: `domain/classifier/ImageClassifier` is the abstraction; `classifierModule`
  currently binds `data/classifier/MockImageClassifier` (deterministic, derived from cheap
  `Bitmap` properties, picks from whatever species are actually seeded in Room — not a
  hardcoded list). `data/classifier/TfLiteImageClassifier` is a documented-but-not-functional
  stub for the real model (not bound in DI). **Swap point:** once a real `.tflite` model +
  `labels.txt` exist, change one line in `classifierModule` (`app/di/AppModules.kt`) from
  `MockImageClassifier` to `TfLiteImageClassifier` — no other file changes. Confidence threshold
  lives in `domain/classifier/ClassificationConfig.MIN_CONFIDENCE_THRESHOLD` (currently `0.65f`,
  a placeholder pending real model metrics).
  - **Note:** `org.tensorflow:tensorflow-lite-support` is deliberately NOT a dependency (and
    neither is its LiteRT replacement) — both have an upstream packaging bug where two of their
    artifacts declare the same AndroidManifest namespace, which AGP 9's stricter uniqueness
    check rejects. Only the core `org.tensorflow:tensorflow-lite` interpreter is used; image
    preprocessing for the real classifier will need to be done manually via `ByteBuffer` instead
    of the support library's helper classes.
- Accounts (in progress on `feature/login`): optional login via Supabase Auth (email + password
  and Google). Server side lives in `supabase/` (see its README): `profiles` table (RLS, created by
  a trigger on `auth.users`, `ON DELETE CASCADE`) and the `delete-account` Edge Function, which
  hard-deletes the caller's account with the service_role (never shipped in the app). Observations
  stay local; deleting the account must also wipe `observation` + `filesDir/observations/`. Any
  future server table with user data must reference `auth.users(id) ON DELETE CASCADE`.
  Client side, in `shared/commonMain`: `domain/auth/` (`AuthRepository`, errors as
  `AuthException(AuthError)`, input validation), `data/auth/SupabaseAuthRepository` +
  `createBioScannerSupabaseClient` (supabase-kt **3.6.0**, pinned: 3.7+ is built with Kotlin 2.4;
  Ktor engine OkHttp on Android, Darwin on iOS; session in SharedPreferences / NSUserDefaults),
  `PlatformCredentialsCleaner` (Android: `app/data/auth/GoogleCredentialCleaner`; iOS: `None` for
  now), and `DeleteAccountUseCase` (server first, then local data). Sign-up confirmation and password reset
  use emailed numeric codes typed in the app (no deep links) — **currently off**: without a custom
  SMTP the free tier can't send them, so `enable_confirmations = false` in `supabase/config.toml`
  (templates commented out) and `auth.emailCodes=false` in `local.properties` hides "Esqueci minha
  senha". Turn all three back on together once SMTP exists. Passwords need 8+ chars with letters
  and digits (`password_requirements`, mirrored in `validatePassword`). Credentials come from
  `local.properties` (`supabase.url`, `supabase.anonKey`) → `BuildConfig`; without them account
  actions fail with `AuthError.NOT_CONFIGURED` and the rest of the app works.
  UI: `presentation/auth/` — one `Routes.AUTH` screen whose `AuthViewModel` walks `AuthStep`s
  (sign in → sign up → confirm code / forgot → reset) and emits `done` once `currentUser` is
  non-null; the user card in `RecordsScreen` shows sign in, or sign out / delete account (with
  confirmation). Google uses Credential Manager (`GetSignInWithGoogleOption` + `GoogleNonce`:
  SHA-256 to Google, raw to Supabase); the button only shows when `google.webClientId` is set in
  `local.properties`. `googleid` is pinned to **1.1.1** (1.2.x is built with Kotlin 2.4, same issue
  as supabase-kt). Instrumented tests override `AuthRepository` (always signed out).
- Test APK distribution: APKs go to GitHub **Releases** (prerelease, tag `v<versionName>-teste`,
  e.g. `v1.1-teste`), never committed. Release builds check that repo's releases on startup
  (`data/update/GitHubUpdateChecker`, list endpoint because `/releases/latest` skips prereleases)
  and `presentation/update/UpdatePrompt` offers the newer APK, opened in the browser; debug builds
  don't check (`UPDATE_REPOSITORY` is empty). To publish: bump `versionCode` **and** `versionName`,
  `assembleRelease`, `zipalign` + `apksigner` with `~/.android/debug.keystore` (no release key yet —
  always sign on this machine, or installed copies can't update in place), then
  `gh release create v<versionName>-teste BioScanner.apk --latest` (**not** prerelease, and the
  asset always named `BioScanner.apk`: the README's download button points to
  `releases/latest/download/BioScanner.apk`, which skips prereleases).
- Async: Kotlin Coroutines + Flow (`SpeciesRepository` exposes data via `Flow`).
- Navigation: Navigation Compose, routes in `presentation/navigation/Routes.kt`.
- Glossary "Por região" tab (`presentation/glossary/region/`): Canvas-drawn map of the 27
  Brazilian states (tap or UF chips) listing snakes recorded in each state, with photo; tapping
  opens the species Detail screen. State outlines come from `assets/regions/br_states.json`
  (not Room); the species list is a Room join. Photos are decoded from assets by
  `presentation/common/SpeciesImage` (small LRU `ImageCache`, no image library).
- Species data pipeline: `tools/data/especies.csv` (`;`-separated, Portuguese headers) is the
  hand-edited source of truth for curated fields. `python3 tools/data/build_species_data.py`
  (`--refresh` to re-query GBIF/IBGE, otherwise uses `tools/data/.cache/`) appends rows for new
  GBIF species to the CSV without touching existing ones, validates it, downloads/converts photos
  (needs ImageMagick) and writes `assets/seed/species_seed.json`, `assets/regions/br_states.json`
  and `assets/images/species/*.webp`. Never edit those outputs by hand. The script docstring
  explains the non-obvious bits (no "Serpentes" taxon in the GBIF backbone → queries by family
  keys; state filter by `gadmGid`; photo license whitelist).

## Product/UX rules

- Always show a visible safety disclaimer: automatic identification is a suggestion, not a
  substitute for expert/medical evaluation — especially relevant for suspected snakebites.
- Prefer a working MVP first: camera → classification → species description. Secondary features
  (e.g. identification history) come after the MVP works end-to-end.

## Working style

- Move in small steps; confirm plans before generating large amounts of code, especially around
  the database schema and the ML integration.
- Comment non-obvious decisions in Portuguese (the user's language).
