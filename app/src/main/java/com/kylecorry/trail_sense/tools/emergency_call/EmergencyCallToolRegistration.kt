package com.kylecorry.trail_sense.tools.emergency_call

import android.content.Context
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.tools.emergency_call.quickactions.QuickActionEmergencyCall
import com.kylecorry.trail_sense.tools.tools.infrastructure.Tool
import com.kylecorry.trail_sense.tools.tools.infrastructure.ToolCategory
import com.kylecorry.trail_sense.tools.tools.infrastructure.ToolQuickAction
import com.kylecorry.trail_sense.tools.tools.infrastructure.ToolRegistration
import com.kylecorry.trail_sense.tools.tools.infrastructure.Tools

object EmergencyCallToolRegistration : ToolRegistration {
    override fun getTool(context: Context): Tool {
        return Tool(
            id = Tools.EMERGENCY_CALL,
            name = context.getString(R.string.tool_emergency_call_title),
            icon = R.drawable.ic_emergency_call,
            navAction = R.id.emergencyCallFragment,
            category = ToolCategory.Signaling,
            description = context.getString(R.string.tool_emergency_call_summary),
            settingsNavAction = R.id.emergencySettingsFragment,
            additionalNavigationIds = listOf(R.id.emergencySettingsFragment),
            quickActions = listOf(
                ToolQuickAction(
                    Tools.QUICK_ACTION_EMERGENCY_CALL,
                    context.getString(R.string.tool_emergency_call_title),
                    ::QuickActionEmergencyCall
                )
            )
        )
    }
}
