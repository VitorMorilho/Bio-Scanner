package com.bioscanner.data.observation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.bioscanner.domain.model.CaptureMetadata
import com.bioscanner.domain.model.GeoPoint
import com.bioscanner.domain.model.MetadataOrigin
import com.bioscanner.domain.observation.CaptureMetadataProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.resume

// Última posição conhecida mais nova que isso é usada direto, sem ligar o GPS.
private const val MAX_LAST_KNOWN_AGE_MS = 2 * 60 * 1000L

// No mato o GPS pode demorar; depois disso fica a última posição conhecida (ou nenhuma).
private const val LOCATION_TIMEOUT_MS = 15_000L

/**
 * Usa o LocationManager da plataforma em vez do Fused Location do Google Play
 * Services: evita uma dependência nova e funciona em aparelhos sem Play Services.
 * Sem permissão de localização, o registro é salvo sem posição.
 */
class AndroidCaptureMetadataProvider(
    private val context: Context,
) : CaptureMetadataProvider {

    override suspend fun forCameraPhoto(): CaptureMetadata {
        val location = currentLocation()
        return CaptureMetadata(
            takenAt = System.currentTimeMillis(),
            takenAtOrigin = MetadataOrigin.DEVICE,
            location = location,
            locationOrigin = location?.let { MetadataOrigin.DEVICE },
        )
    }

    override suspend fun forGalleryPhoto(photoUri: String): CaptureMetadata {
        val exif = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(photoUri))?.use { ExifInterface(it) }
            }.getOrNull()
        }
        val exifTime = exif?.let(::readExifTime)
        // O seletor de fotos do Android costuma remover a localização do EXIF por
        // privacidade; nesse caso cai para a posição atual, marcada como DEVICE.
        val exifLocation = exif?.let(::readExifLocation)
        val location = exifLocation ?: currentLocation()
        return CaptureMetadata(
            takenAt = exifTime ?: System.currentTimeMillis(),
            takenAtOrigin = if (exifTime != null) MetadataOrigin.PHOTO else MetadataOrigin.DEVICE,
            location = location,
            locationOrigin = when {
                exifLocation != null -> MetadataOrigin.PHOTO
                location != null -> MetadataOrigin.DEVICE
                else -> null
            },
        )
    }

    private fun readExifTime(exif: ExifInterface): Long? {
        val raw = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
            ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
            ?: return null
        // Formato EXIF, no fuso do aparelho que tirou a foto.
        return try {
            SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US).parse(raw)?.time
        } catch (_: ParseException) {
            null
        }
    }

    private fun readExifLocation(exif: ExifInterface): GeoPoint? {
        val latLong = FloatArray(2)
        @Suppress("DEPRECATION") // a versão com DoubleArray só existe a partir da API 29
        if (!exif.getLatLong(latLong)) return null
        return GeoPoint(latLong[0].toDouble(), latLong[1].toDouble())
    }

    @SuppressLint("MissingPermission") // checado em hasLocationPermission()
    private suspend fun currentLocation(): GeoPoint? {
        if (!hasLocationPermission()) return null
        val manager = context.getSystemService(LocationManager::class.java) ?: return null
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }
        if (providers.isEmpty()) return null

        val lastKnown = providers.mapNotNull { manager.getLastKnownLocation(it) }.maxByOrNull { it.time }
        if (lastKnown != null && System.currentTimeMillis() - lastKnown.time < MAX_LAST_KNOWN_AGE_MS) {
            return lastKnown.toGeoPoint()
        }

        val fresh = withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            suspendCancellableCoroutine<Location?> { cont ->
                val signal = androidx.core.os.CancellationSignal()
                cont.invokeOnCancellation { signal.cancel() }
                LocationManagerCompat.getCurrentLocation(
                    manager,
                    providers.first(),
                    signal,
                    ContextCompat.getMainExecutor(context),
                ) { location -> if (cont.isActive) cont.resume(location) }
            }
        }
        return (fresh ?: lastKnown)?.toGeoPoint()
    }

    private fun hasLocationPermission() = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ).any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

    private fun Location.toGeoPoint() = GeoPoint(latitude, longitude)
}
