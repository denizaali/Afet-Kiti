package com.kylecorry.trail_sense.tools.emergency_call.infrastructure.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyContactDao {
    @Query("SELECT * FROM emergency_contacts ORDER BY name ASC")
    fun getAll(): Flow<List<EmergencyContactEntity>>

    @Query("SELECT * FROM emergency_contacts ORDER BY name ASC")
    suspend fun getAllSync(): List<EmergencyContactEntity>

    @Query("SELECT * FROM emergency_contacts WHERE _id = :id LIMIT 1")
    suspend fun get(id: Long): EmergencyContactEntity?

    @Upsert
    suspend fun upsert(entity: EmergencyContactEntity): Long

    @Delete
    suspend fun delete(entity: EmergencyContactEntity)
}
