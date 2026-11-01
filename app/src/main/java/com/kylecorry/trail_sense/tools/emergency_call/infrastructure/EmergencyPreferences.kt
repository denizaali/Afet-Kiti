package com.kylecorry.trail_sense.tools.emergency_call.infrastructure

import android.content.Context
import com.kylecorry.andromeda.preferences.BooleanPreference
import com.kylecorry.andromeda.preferences.StringPreference
import com.kylecorry.trail_sense.settings.infrastructure.PreferenceRepo

class EmergencyPreferences(context: Context) : PreferenceRepo(context) {

    var emergencyNumber: String by StringPreference(
        cache,
        "pref_emergency_number",
        "112"
    )

    var smsTemplate: String by StringPreference(
        cache,
        "pref_emergency_sms_template",
        DEFAULT_SMS_TEMPLATE
    )

    var sendSmsOnCall: Boolean by BooleanPreference(
        cache,
        "pref_emergency_send_sms_on_call",
        true
    )

    var hasAcceptedKvkk: Boolean by BooleanPreference(
        cache,
        "pref_emergency_kvkk_accepted",
        false
    )

    companion object {
        const val DEFAULT_SMS_TEMPLATE = "Acil durumdayım. Konumum: {konum} - {tarih}"
    }
}
