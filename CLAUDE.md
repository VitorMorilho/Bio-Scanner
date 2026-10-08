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

iOS is not a current priority. Keep business/domain logic free of Android-UI-only assumptions
where reasonable, in case of a future Kotlin Multiplatform migration — but do not implement
anything iOS-related now.

## Project status

Main flow: Home → Capture → Register (photo + date/time + location, "Salvar nos meus
registros") → Meus registros. A photo becomes an *observation* (registro) saved to the user's
records; there is **no species identification/analysis in the flow anymore** (the user dropped
both instant identification and "send for analysis"). The classifier stack (`ImageClassifier`,
`MockImageClassifier`, `ClassifyAndLookupSpeciesUseCase`, `ConfidenceBadge`) is kept but
currently unused by any screen, pending the real model. Bottom nav: Registrar · Glossário
(species search + map by state) · Meus registros (mocked user area listing the observations —
no login yet). No real
dataset or trained `.tflite` model exists yet. Species content is real: ~380 Brazilian snake
species from GBIF, one open-license photo each (iNaturalist), and hand-curated text for the
species filled in `tools/data/especies.csv` (see "Species data pipeline").

Not yet a git repo with commits (a plain `git init` was run, but no author identity is
configured — set `user.name`/`user.email` locally in this repo before committing).

## Commands

This is a Gradle project; use the wrapper (`./gradlew`) from the repo root.

- Build debug APK: `./gradlew assembleDebug`
- Run local (JVM) unit tests: `./gradlew test`
- Run a single local unit test class: `./gradlew test --tests "com.bioscanner.domain.usecase.ClassifyAndLookupSpeciesUseCaseTest"`
- Compile instrumented test sources without a device: `./gradlew assembleDebugAndroidTest`
- Run instrumented tests (needs a connected device/emulator): `./gradlew connectedAndroidTest`
- Lint: `./gradlew lint`
- Full check (lint + tests): `./gradlew check`

## Architecture

- Single module: `app` (applicationId/namespace `com.bioscanner`).
- UI is Jetpack Compose only (no XML layouts).
- Theming lives in `app/src/main/java/com/bioscanner/ui/theme/` (`Color.kt`, `Theme.kt`, `Type.kt`)
  — `BioScannerTheme` wraps all Compose content and should be reused for any new screens.
- Dependency versions are centralized in the Gradle version catalog at
  `gradle/libs.versions.toml`; add new dependencies there rather than hardcoding versions in
  `app/build.gradle.kts`.
- Build config: compileSdk 36, minSdk 24, targetSdk 36, Kotlin 2.0.21 (compose compiler plugin;
  AGP 9's built-in Kotlin, not `org.jetbrains.kotlin.android`), AGP 9.1.1, Java 11
  source/target compatibility, KSP `2.3.12` (decoupled version, not Kotlin-prefixed — AGP 9's
  built-in Kotlin rejects the old `<kotlin>-<ksp>` versioning), Hilt `2.59.2` (first Hilt
  version with real AGP 9 support; anything older fails with "Android BaseExtension not found"
  or a `ComponentTreeDeps` runtime-class error).
- MVVM + Clean Architecture under `com.bioscanner`: `domain/` (model, repository interface,
  `ImageClassifier` interface, use cases — no Android/Room/Hilt imports except
  `android.graphics.Bitmap` in the classifier interface, kept for simplicity per the scope note
  below), `data/` (Room, seed loading, repository impl, classifiers), `presentation/`
  (navigation + one package per screen: `home`, `capture`, `result`, `detail`, plus `common` for
  shared composables like `DisclaimerBanner`), `di/` (Hilt modules).
- Dependency injection: Hilt. `ClassifierModule` and `RepositoryModule` use `@Binds` on interfaces;
  `DatabaseModule` provides the Room database + DAO; `DispatchersModule` provides an
  `@ApplicationScope` `CoroutineScope` for DB seeding.
- Camera: CameraX (`presentation/capture/CameraPreviewView.kt`) writes captures straight to
  `MediaStore` (`Pictures/BioScanner`) via `ImageCapture.OutputFileOptions` — no `FileProvider`
  needed, since `MediaStore` already returns an app-usable `content://` URI and needs no extra
  storage permission on scoped-storage-era Android. Gallery uses
  `ActivityResultContracts.PickVisualMedia()` (modern photo picker). Camera permission is
  optional at runtime — if denied, gallery picking still works.
- Local database: Room v3 (`data/local/db/`): flat `species` table (every field 1:1 with a
  species; curated fields are nullable) + `species_region` N:N join (species × UF, with GBIF
  occurrence counts) + `observation` (the user's records; its `status`/`speciesId`/`confidence` columns are leftovers
  from the dropped analysis flow, kept to avoid another migration — `status` is always `SAVED`). `classifierLabel` is a separate column
  from `id`, since a real model's label taxonomy may not match the DB's internal slugs 1:1.
  `species`/`species_region` are asset-derived: `DatabaseCallback.onOpen` replaces them whenever
  the seed's `version` hash differs from the one stored in SharedPreferences (it never touches
  `observation`). **`observation` is user data: every schema change needs a `Migration`** (see
  `MIGRATION_2_3` and `androidTest/.../MigrationTest`, which validates against
  `app/schemas/`); destructive fallback is only allowed from v1. `observation.speciesId` is
  deliberately not a FK, since reseeding deletes and reinserts species.
- Observations (`domain/observation/` ports, `data/observation/` impls, bound in
  `ObservationModule`): `CaptureMetadataProvider` (camera photo → now + device location; gallery
  photo → EXIF date/location, falling back to device; platform `LocationManager`, no Play
  Services; location permission is optional), `PhotoStorage` (copies the photo into
  `filesDir/observations/`, downscaled to 1600 px and EXIF-rotated — picker URIs expire). The Android photo picker usually strips EXIF location, so
  gallery photos often get the device location instead (the UI says which origin each field has).
- ML / recognition: `domain/classifier/ImageClassifier` is the abstraction; `ClassifierModule`
  currently binds `data/classifier/MockImageClassifier` (deterministic, derived from cheap
  `Bitmap` properties, picks from whatever species are actually seeded in Room — not a
  hardcoded list). `data/classifier/TfLiteImageClassifier` is a documented-but-not-functional
  stub for the real model (not bound in DI). **Swap point:** once a real `.tflite` model +
  `labels.txt` exist, change one `@Binds` parameter type in `ClassifierModule.kt` from
  `MockImageClassifier` to `TfLiteImageClassifier` — no other file changes. Confidence threshold
  lives in `domain/classifier/ClassificationConfig.MIN_CONFIDENCE_THRESHOLD` (currently `0.65f`,
  a placeholder pending real model metrics).
  - **Note:** `org.tensorflow:tensorflow-lite-support` is deliberately NOT a dependency (and
    neither is its LiteRT replacement) — both have an upstream packaging bug where two of their
    artifacts declare the same AndroidManifest namespace, which AGP 9's stricter uniqueness
    check rejects. Only the core `org.tensorflow:tensorflow-lite` interpreter is used; image
    preprocessing for the real classifier will need to be done manually via `ByteBuffer` instead
    of the support library's helper classes.
- Async: Kotlin Coroutines + Flow (`SpeciesRepository` exposes data via `Flow`).
- Navigation: Navigation Compose, routes in `presentation/navigation/Routes.kt`.
- Glossary "Por região" tab (`presentation/glossary/region/`): Canvas-drawn map of the 27
  Brazilian states (tap or UF chips) listing snakes recorded in each state, with photo; tapping
  opens the species Detail screen. State outlines come from `assets/regions/br_states.json`
  (not Room); the species list is a Room join. Photos are decoded from assets by
  `presentation/common/SpeciesImage` (LruCache, no image library).
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
