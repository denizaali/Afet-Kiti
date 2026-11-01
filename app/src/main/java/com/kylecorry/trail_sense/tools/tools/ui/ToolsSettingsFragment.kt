package com.kylecorry.trail_sense.tools.tools.ui

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import com.kylecorry.andromeda.fragments.AndromedaPreferenceFragment
import com.kylecorry.andromeda.pickers.Pickers
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.shared.UserPreferences
import com.kylecorry.trail_sense.shared.requireMainActivity
import com.kylecorry.trail_sense.tools.tools.infrastructure.Tools
import com.kylecorry.trail_sense.tools.tools.ui.sort.AlphabeticalToolSort

import androidx.navigation.fragment.findNavController
import com.kylecorry.trail_sense.shared.navigateWithAnimation

class ToolsSettingsFragment : AndromedaPreferenceFragment() {

    private val prefs by lazy { UserPreferences(requireContext()) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.tool_preferences, rootKey)
        bindQuickActionPreference()
        bindWidgetPreference()
        bindBottomNavPreferences()
    }

    private fun bindBottomNavPreferences() {
        onClick(findPreference(getString(R.string.pref_bottom_nav_settings))) {
            findNavController().navigateWithAnimation(R.id.action_tools_settings_to_bottom_navigation_settings)
        }
    }

    private fun bindQuickActionPreference() {
        onClick(findPreference(getString(R.string.pref_tool_quick_action_header_key))) {
            val potentialActions = Tools.getQuickActions(requireContext())
                .filterNot { it.id == Tools.QUICK_ACTION_NONE }

            val selected = prefs.toolQuickActions

            val selectedIndices = potentialActions.mapIndexedNotNull { index, action ->
                if (selected.contains(action.id)) index else null
            }

            Pickers.items(
                requireContext(),
                getString(R.string.quick_actions),
                potentialActions.map { it.name },
                selectedIndices
            ) {
                if (it != null) {
                    prefs.toolQuickActions = it.map { potentialActions[it].id }
                }
            }
        }
    }

    private fun bindWidgetPreference() {
        onClick(findPreference(getString(R.string.pref_tool_widgets_header_key))) {
            val tools = Tools.getTools(requireContext())
            val potentialWidgets = tools
                .flatMap { it.widgets }
                .filter { it.isEnabled(requireContext()) && it.canPlaceInApp }
                .distinctBy { it.id }
                .sortedBy { it.name }

            val selected = prefs.toolWidgets

            val selectedIndices = potentialWidgets.mapIndexedNotNull { index, widget ->
                if (selected.contains(widget.id)) index else null
            }

            Pickers.items(
                requireContext(),
                getString(R.string.widget_pref_title),
                potentialWidgets.map { it.name },
                selectedIndices
            ) {
                if (it != null) {
                    prefs.toolWidgets = it.map { potentialWidgets[it].id }
                }
            }
        }
    }
}
