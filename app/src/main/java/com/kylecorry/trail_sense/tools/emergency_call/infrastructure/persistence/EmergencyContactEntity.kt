package com.kylecorry.trail_sense.tools.emergency_call.infrastructure.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kylecorry.trail_sense.tools.emergency_call.domain.EmergencyContact

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "phone") val phone: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0

    fun toEmergencyContact(): EmergencyContact {
        return EmergencyContact(
            id = id,
            name = name,
            phone = phone
        )
    }

    companion object {
        fun from(contact: EmergencyContact): EmergencyContactEntity {
            return EmergencyContactEntity(
                name = contact.name,
                phone = contact.phone
            ).also {
                it.id = contact.id
            }
        }
    }
}
