package com.kylecorry.trail_sense.tools.emergency_call.quickactions

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.shared.QuickActionButton
import com.kylecorry.trail_sense.shared.openTool
import com.kylecorry.trail_sense.shared.quickactions.QuickActionButtonView
import com.kylecorry.trail_sense.tools.tools.infrastructure.Tools

class QuickActionEmergencyCall(btn: QuickActionButtonView, fragment: Fragment) :
    QuickActionButton(btn, fragment) {

    override fun onCreate() {
        super.onCreate()
        setIcon(R.drawable.ic_emergency_call)
    }

    override fun onClick() {
        super.onClick()
        fragment.findNavController().openTool(Tools.EMERGENCY_CALL)
    }

    override fun onLongClick(): Boolean {
        super.onLongClick()
        fragment.findNavController().openTool(Tools.EMERGENCY_CALL)
        return true
    }
}
