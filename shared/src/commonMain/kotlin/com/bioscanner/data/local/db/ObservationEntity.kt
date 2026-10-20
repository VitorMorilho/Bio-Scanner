package com.bioscanner.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
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
@Entity(tableName = "observation", indices = [Index(value = ["remoteId"], unique = true)])
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
    /** Id (uuid) do registro na nuvem. Null só em registros anteriores à v5, até a sincronização. */
    val remoteId: String? = null,
    /** Dono (id do Supabase Auth). Null = feito sem conta; passa a ser da conta no próximo login. */
    val userId: String? = null,
    /** Um de [SyncState]. O default no SQL tem que bater com o ADD COLUMN de MIGRATION_4_5. */
    @ColumnInfo(defaultValue = SyncState.LOCAL)
    val syncState: String = SyncState.LOCAL,
)

/** Situação do registro em relação à nuvem (coluna syncState). */
object SyncState {
    /** Sem conta: só existe no aparelho. */
    const val LOCAL = "LOCAL"

    /** Tem dono, mas ainda não foi enviado (sem internet, ou o envio falhou). */
    const val PENDING_UPLOAD = "PENDING_UPLOAD"

    /** Igual ao da nuvem. */
    const val SYNCED = "SYNCED"

    /** Apagado pelo usuário; some da lista e sai da nuvem na próxima sincronização. */
    const val PENDING_DELETE = "PENDING_DELETE"
}
