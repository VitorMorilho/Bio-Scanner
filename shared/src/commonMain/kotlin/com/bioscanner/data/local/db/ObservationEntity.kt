package com.bioscanner.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Registro do usuário — primeiro dado do banco que NÃO vem dos assets, por
 * isso mudanças nesta tabela exigem Migration (ver BioScannerDatabase).
 *
 * status/speciesId/confidence são da primeira versão (envio para análise),
 * que foi descartada: ficam sem uso para não exigir outra migração. Se a
 * espécie voltar a ser associada ao registro, reaproveitar speciesId — sem FK,
 * porque a tabela species é apagada e recriada a cada seed (DatabaseCallback).
 */
@Entity(tableName = "observation")
data class ObservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val photoPath: String,
    val takenAt: Long,
    val takenAtOrigin: String,
    val latitude: Double?,
    val longitude: Double?,
    val locationOrigin: String?,
    val status: String,
    val speciesId: String?,
    val confidence: Float?,
    val createdAt: Long,
)
