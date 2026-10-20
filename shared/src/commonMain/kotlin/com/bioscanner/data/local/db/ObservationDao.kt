package com.bioscanner.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationDao {
    /** Registros de [userId] (null = os feitos sem conta), sem os que estão sendo apagados. */
    @Query(
        "SELECT * FROM observation WHERE userId IS :userId AND syncState != '${SyncState.PENDING_DELETE}' " +
            "ORDER BY takenAt DESC",
    )
    fun getAllFor(userId: String?): Flow<List<ObservationEntity>>

    @Query("SELECT * FROM observation WHERE id = :id")
    fun getById(id: Long): Flow<ObservationEntity?>

    @Query("SELECT * FROM observation WHERE id = :id")
    suspend fun findById(id: Long): ObservationEntity?

    @Insert
    suspend fun insert(observation: ObservationEntity): Long

    @Query("DELETE FROM observation WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM observation")
    suspend fun deleteAll()

    // ---- Sincronização (ver ObservationSync)

    /** Registros feitos sem conta passam a ser de [userId] e entram na fila de envio. */
    @Query(
        "UPDATE observation SET userId = :userId, syncState = '${SyncState.PENDING_UPLOAD}' WHERE userId IS NULL",
    )
    suspend fun adoptOrphans(userId: String)

    @Query("SELECT * FROM observation WHERE remoteId IS NULL")
    suspend fun findWithoutRemoteId(): List<ObservationEntity>

    @Query("UPDATE observation SET remoteId = :remoteId WHERE id = :id")
    suspend fun setRemoteId(id: Long, remoteId: String)

    @Query("SELECT * FROM observation WHERE userId = :userId AND syncState = :syncState")
    suspend fun findByState(userId: String, syncState: String): List<ObservationEntity>

    @Query("UPDATE observation SET syncState = :syncState WHERE id = :id")
    suspend fun setSyncState(id: Long, syncState: String)

    @Query("SELECT COUNT(*) FROM observation WHERE userId = :userId AND syncState = :syncState")
    fun countByState(userId: String, syncState: String): Flow<Int>

    @Query("SELECT * FROM observation WHERE userId = :userId")
    suspend fun findByUser(userId: String): List<ObservationEntity>

    @Query("DELETE FROM observation WHERE userId = :userId")
    suspend fun deleteByUser(userId: String)
}
