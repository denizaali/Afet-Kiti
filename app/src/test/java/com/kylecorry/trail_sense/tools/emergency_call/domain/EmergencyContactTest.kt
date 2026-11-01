package com.kylecorry.trail_sense.tools.emergency_call.domain

import com.kylecorry.trail_sense.tools.emergency_call.infrastructure.persistence.EmergencyContactEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class EmergencyContactTest {

    @Test
    fun roundTripEntityMapping() {
        val contact = EmergencyContact(
            id = 42L,
            name = "Anne",
            phone = "05321234567"
        )

        val entity = EmergencyContactEntity.from(contact)
        assertEquals(contact.id, entity.id)
        assertEquals(contact.name, entity.name)
        assertEquals(contact.phone, entity.phone)

        val mappedBack = entity.toEmergencyContact()
        assertEquals(contact, mappedBack)
    }

    @Test
    fun defaultValues() {
        val contact = EmergencyContact(
            name = "AFAD",
            phone = "122"
        )
        assertEquals(0L, contact.id)
        assertEquals("AFAD", contact.name)
        assertEquals("122", contact.phone)
    }
}
