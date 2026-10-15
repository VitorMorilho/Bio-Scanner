package com.bioscanner.data.observation

import com.bioscanner.domain.model.CaptureMetadata
import com.bioscanner.domain.model.MetadataOrigin
import com.bioscanner.domain.observation.CaptureMetadataProvider
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Provisório: hora atual e sem local.
 * TODO(iOS): posição pelo CLLocationManager e data/local do EXIF da foto (ImageIO), como o
 * AndroidCaptureMetadataProvider.
 */
@OptIn(ExperimentalTime::class)
class IosCaptureMetadataProvider : CaptureMetadataProvider {

    override suspend fun forCameraPhoto(): CaptureMetadata = now()

    override suspend fun forGalleryPhoto(photoUri: String): CaptureMetadata = now()

    private fun now() = CaptureMetadata(
        takenAt = Clock.System.now().toEpochMilliseconds(),
        takenAtOrigin = MetadataOrigin.DEVICE,
        location = null,
        locationOrigin = null,
    )
}
