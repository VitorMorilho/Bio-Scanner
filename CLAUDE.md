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

MVP skeleton is implemented end-to-end (Home → Capture → Result → Detail) using **mock data**:
~6 hand-authored snake species seeded into Room from `assets/seed/species_seed.json`, and a
deterministic `MockImageClassifier` standing in for the real model (see "ML / recognition"
below). No real dataset or trained `.tflite` model exists yet — pending the user providing the
image dataset path and/or training the model. No reference photos are bundled yet either
(`SpeciesEntity.referenceImagePath` is populated in the seed data but the actual JPEGs under
`assets/images/reference/` don't exist yet; the UI doesn't try to load them).

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
- Local database: Room, single flat `SpeciesEntity` table (`data/local/db/`) — deliberately not
  normalized; every field is 1:1 with a species, so splitting into related tables would only add
  join complexity for no current benefit. `classifierLabel` is a separate column from `id`,
  since a real model's label taxonomy may not match the DB's internal slugs 1:1. Seeded on first
  DB creation (`DatabaseCallback.onCreate`) from `assets/seed/species_seed.json`, parsed with
  kotlinx.serialization.
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

## Product/UX rules

- Always show a visible safety disclaimer: automatic identification is a suggestion, not a
  substitute for expert/medical evaluation — especially relevant for suspected snakebites.
- Prefer a working MVP first: camera → classification → species description. Secondary features
  (e.g. identification history) come after the MVP works end-to-end.

## Working style

- Move in small steps; confirm plans before generating large amounts of code, especially around
  the database schema and the ML integration.
- Comment non-obvious decisions in Portuguese (the user's language).
