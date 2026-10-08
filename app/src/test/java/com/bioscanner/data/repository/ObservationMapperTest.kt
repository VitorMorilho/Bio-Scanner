package com.bioscanner.data.repository

import com.bioscanner.data.local.db.ObservationEntity
import com.bioscanner.domain.model.GeoPoint
import com.bioscanner.domain.model.MetadataOrigin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ObservationMapperTest {

    private fun entity(
        latitude: Double? = -15.6,
        longitude: Double? = -56.1,
        locationOrigin: String? = "PHOTO",
    ) = ObservationEntity(
        id = 7,
        photoPath = "/x.jpg",
        takenAt = 123,
        takenAtOrigin = "PHOTO",
        latitude = latitude,
        longitude = longitude,
        locationOrigin = locationOrigin,
        status = "SAVED",
        speciesId = null,
        confidence = null,
        createdAt = 456,
    )

    @Test
    fun `maps location and origins`() {
        val o = entity().toDomain()

        assertEquals(GeoPoint(-15.6, -56.1), o.metadata.location)
        assertEquals(MetadataOrigin.PHOTO, o.metadata.takenAtOrigin)
        assertEquals(MetadataOrigin.PHOTO, o.metadata.locationOrigin)
    }

    @Test
    fun `missing coordinates mean no location`() {
        val o = entity(latitude = null, longitude = null, locationOrigin = null).toDomain()

        assertNull(o.metadata.location)
        assertNull(o.metadata.locationOrigin)
    }
}
