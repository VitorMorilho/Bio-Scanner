package com.bioscanner.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "species")
data class SpeciesEntity(
    @PrimaryKey val id: String,
    val scientificName: String,
    val popularName: String,
    val family: String,
    val category: String,
    val venomous: Boolean,
    val riskLevel: String,
    val description: String,
    val geographicDistribution: String,
    val firstAidProcedure: String,
    val referenceImagePath: String?,
    val classifierLabel: String?,
)
