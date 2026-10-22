package com.kylecorry.trail_sense.tools.emergency_call.infrastructure.persistence

import android.annotation.SuppressLint
import android.content.Context
import com.kylecorry.luna.concurrency.onIO
import com.kylecorry.trail_sense.main.persistence.AppDatabase
import com.kylecorry.trail_sense.tools.emergency_call.domain.EmergencyContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class EmergencyContactRepo private constructor(context: Context) {

    private val dao = AppDatabase.getInstance(context).emergencyContactDao()

    fun getAll(): Flow<List<EmergencyContact>> = dao.getAll()
        .map { it.map { entity -> entity.toEmergencyContact() } }
        .flowOn(Dispatchers.IO)

    suspend fun getAllSync(): List<EmergencyContact> = onIO {
        dao.getAllSync().map { it.toEmergencyContact() }
    }

    suspend fun get(id: Long): EmergencyContact? = onIO {
        dao.get(id)?.toEmergencyContact()
    }

    suspend fun add(model: EmergencyContact): Long = onIO {
        dao.upsert(EmergencyContactEntity.from(model))
    }

    suspend fun delete(model: EmergencyContact) = onIO {
        dao.delete(EmergencyContactEntity.from(model))
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: EmergencyContactRepo? = null

        @Synchronized
        fun getInstance(context: Context): EmergencyContactRepo {
            if (instance == null) {
                instance = EmergencyContactRepo(context.applicationContext)
            }
            return instance!!
        }
    }
}
