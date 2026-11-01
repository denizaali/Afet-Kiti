package com.kylecorry.trail_sense.tools.emergency_call.infrastructure

import android.Manifest
import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import com.kylecorry.andromeda.permissions.Permissions
import com.kylecorry.luna.concurrency.onIO
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.shared.sensors.LocationSubsystem
import com.kylecorry.trail_sense.shared.sensors.SensorService
import com.kylecorry.trail_sense.tools.emergency_call.domain.EmergencyContact
import com.kylecorry.trail_sense.tools.emergency_call.infrastructure.persistence.EmergencyContactRepo
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class SmsSendResult(
    val contact: EmergencyContact,
    val success: Boolean,
    val errorMessage: String? = null
)

class EmergencySmsSender(private val context: Context) {

    private val prefs by lazy { EmergencyPreferences(context) }
    private val repo by lazy { EmergencyContactRepo.getInstance(context) }
    private val locationSubsystem by lazy { LocationSubsystem.getInstance(context) }

    @Suppress("TooGenericExceptionCaught")
    suspend fun sendEmergencySms(): List<SmsSendResult> = onIO {
        if (!Permissions.hasPermission(context, Manifest.permission.SEND_SMS)) {
            return@onIO emptyList()
        }

        val contacts = repo.getAllSync()
        if (contacts.isEmpty()) {
            return@onIO emptyList()
        }

        val message = prepareMessage()

        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }

        contacts.map { contact ->
            try {
                val parts = smsManager.divideMessage(message)
                if (parts.size > 1) {
                    smsManager.sendMultipartTextMessage(contact.phone, null, parts, null, null)
                } else {
                    smsManager.sendTextMessage(contact.phone, null, message, null, null)
                }
                SmsSendResult(contact, true)
            } catch (e: Exception) {
                SmsSendResult(contact, false, e.localizedMessage ?: "Gönderilemedi")
            }
        }
    }

    suspend fun prepareMessage(): String {
        val locationText = getLocationString()
        val dateText = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))

        var template = prefs.smsTemplate
        if (template.isBlank()) {
            template = EmergencyPreferences.DEFAULT_SMS_TEMPLATE
        }

        return template
            .replace("{konum}", locationText, ignoreCase = true)
            .replace("{location}", locationText, ignoreCase = true)
            .replace("{tarih}", dateText, ignoreCase = true)
            .replace("{date}", dateText, ignoreCase = true)
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private suspend fun getLocationString(): String {
        var coordinate: Coordinate? = null
        var isRecent = false

        if (Permissions.canGetFineLocation(context)) {
            try {
                val gps = SensorService(context).getGPS()
                withTimeoutOrNull(4000) {
                    gps.read()
                }
                if (gps.hasValidReading && gps.location != Coordinate.zero) {
                    coordinate = gps.location
                    isRecent = true
                }
                gps.stop(null)
            } catch (_: Exception) {
                // Ignore GPS errors
            }
        }

        if (coordinate == null) {
            val cached = locationSubsystem.location
            if (cached != Coordinate.zero) {
                coordinate = cached
                isRecent = false
            }
        }

        return if (coordinate != null) {
            val note = if (isRecent) "" else " (Son bilinen)"
            "https://maps.google.com/?q=${coordinate.latitude},${coordinate.longitude}$note"
        } else {
            context.getString(R.string.emergency_location_unavailable)
        }
    }
}
