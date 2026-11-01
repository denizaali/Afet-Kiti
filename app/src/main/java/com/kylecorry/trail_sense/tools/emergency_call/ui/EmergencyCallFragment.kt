package com.kylecorry.trail_sense.tools.emergency_call.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kylecorry.andromeda.alerts.Alerts
import com.kylecorry.andromeda.alerts.toast
import com.kylecorry.andromeda.fragments.BoundFragment
import com.kylecorry.andromeda.fragments.inBackground
import com.kylecorry.andromeda.permissions.Permissions
import com.kylecorry.sol.units.Coordinate
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.databinding.DialogKvkkBinding
import com.kylecorry.trail_sense.databinding.FragmentEmergencyCallBinding
import com.kylecorry.trail_sense.shared.FormatService
import com.kylecorry.trail_sense.shared.haptics.HapticSubsystem
import com.kylecorry.trail_sense.shared.sensors.LocationSubsystem
import com.kylecorry.trail_sense.tools.emergency_call.domain.EmergencyContact
import com.kylecorry.trail_sense.tools.emergency_call.infrastructure.EmergencyPreferences
import com.kylecorry.trail_sense.tools.emergency_call.infrastructure.EmergencySmsSender
import com.kylecorry.trail_sense.tools.emergency_call.infrastructure.persistence.EmergencyContactRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class EmergencyCallFragment : BoundFragment<FragmentEmergencyCallBinding>() {

    private val prefs by lazy { EmergencyPreferences(requireContext()) }
    private val repo by lazy { EmergencyContactRepo.getInstance(requireContext()) }
    private val smsSender by lazy { EmergencySmsSender(requireContext()) }
    private val haptics by lazy { HapticSubsystem.getInstance(requireContext()) }
    private val locationSubsystem by lazy { LocationSubsystem.getInstance(requireContext()) }
    private val formatter by lazy { FormatService.getInstance(requireContext()) }

    private var contacts: List<EmergencyContact> = emptyList()
    private var holdJob: Job? = null
    private var isHolding = false

    override fun generateBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentEmergencyCallBinding {
        return FragmentEmergencyCallBinding.inflate(layoutInflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnEmergencySettings.setOnClickListener {
            navigateToSettings()
        }

        binding.btnSendEmergencySms.setOnClickListener {
            triggerEmergencySmsManual()
        }

        setupCallButton()
        observeContacts()
        updateLocationStatus()

        if (!prefs.hasAcceptedKvkk) {
            showKvkkConsentDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        updateLocationStatus()
    }

    private fun updateUI() {
        val number = prefs.emergencyNumber
        binding.emergencyNumberStatus.text = getString(R.string.emergency_default_number_label, number)
        binding.btnCallText.text = if (number == "112") {
            getString(R.string.emergency_call_112_button)
        } else {
            getString(R.string.emergency_call_button_format, number)
        }
    }

    private fun observeContacts() {
        viewLifecycleOwner.lifecycleScope.launch {
            repo.getAll().collectLatest { list ->
                contacts = list
                binding.emergencyContactsStatus.text =
                    getString(R.string.emergency_contacts_count, contacts.size)
            }
        }
    }

    private fun updateLocationStatus() {
        if (!Permissions.canGetFineLocation(requireContext())) {
            binding.emergencyLocationStatus.text = getString(
                R.string.emergency_location_permission_denied
            )
            return
        }

        val cachedLoc = locationSubsystem.location
        if (cachedLoc != Coordinate.zero) {
            binding.emergencyLocationStatus.text = getString(
                R.string.location
            ) + ": " + formatter.formatLocation(cachedLoc)
        } else {
            binding.emergencyLocationStatus.text = getString(R.string.loading)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupCallButton() {
        val holdDurationMs = 2000L
        val intervalMs = 50L

        binding.btnEmergencyCall.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isHolding = true
                    haptics.click()
                    binding.callHoldProgress.isVisible = true
                    binding.callHoldProgress.progress = 0

                    holdJob = viewLifecycleOwner.lifecycleScope.launch {
                        var elapsed = 0L
                        while (isActive && elapsed < holdDurationMs) {
                            delay(intervalMs)
                            elapsed += intervalMs
                            binding.callHoldProgress.progress = ((elapsed.toFloat() / holdDurationMs) * 100).toInt()
                        }
                        if (isActive && isHolding) {
                            haptics.click()
                            binding.callHoldProgress.isVisible = false
                            executeEmergencyCall()
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val wasHolding = isHolding
                    isHolding = false
                    val progress = binding.callHoldProgress.progress
                    holdJob?.cancel()
                    binding.callHoldProgress.isVisible = false
                    binding.callHoldProgress.progress = 0

                    if (wasHolding && progress < 100) {
                        showCallConfirmationDialog()
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    isHolding = false
                    holdJob?.cancel()
                    binding.callHoldProgress.isVisible = false
                    binding.callHoldProgress.progress = 0
                    true
                }
                else -> false
            }
        }
    }

    private fun showCallConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.emergency_call_confirmation_title)
            .setMessage(getString(R.string.emergency_call_confirmation_message, prefs.emergencyNumber))
            .setPositiveButton(R.string.emergency_call_now) { _, _ ->
                executeEmergencyCall()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun executeEmergencyCall() {
        val number = prefs.emergencyNumber
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$number")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            startActivity(dialIntent)
        } catch (e: Exception) {
            toast(getString(R.string.error_occurred))
            return
        }

        if (prefs.sendSmsOnCall && prefs.hasAcceptedKvkk) {
            sendEmergencySmsBackground()
        }
    }

    private fun sendEmergencySmsBackground() {
        if (!Permissions.hasPermission(requireContext(), Manifest.permission.SEND_SMS)) {
            return
        }

        inBackground {
            val results = smsSender.sendEmergencySms()
            val successfulCount = results.count { it.success }
            if (successfulCount > 0) {
                toast(getString(R.string.emergency_sms_all_sent, successfulCount))
            } else if (results.isNotEmpty()) {
                toast(getString(R.string.emergency_sms_none_sent))
            }
        }
    }

    private fun triggerEmergencySmsManual() {
        if (!prefs.hasAcceptedKvkk) {
            showKvkkConsentDialog()
            return
        }

        if (!Permissions.hasPermission(requireContext(), Manifest.permission.SEND_SMS)) {
            requestPermissions(listOf(Manifest.permission.SEND_SMS)) {
                if (Permissions.hasPermission(requireContext(), Manifest.permission.SEND_SMS)) {
                    triggerEmergencySmsManual()
                } else {
                    toast(getString(R.string.emergency_sms_permission_denied))
                }
            }
            return
        }

        if (contacts.isEmpty()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.emergency_contacts_title)
                .setMessage(R.string.emergency_sms_no_contacts)
                .setPositiveButton(R.string.emergency_settings_button) { _, _ ->
                    navigateToSettings()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            return
        }

        toast(getString(R.string.loading))
        inBackground {
            val results = smsSender.sendEmergencySms()
            val successCount = results.count { it.success }
            if (successCount == results.size && results.isNotEmpty()) {
                toast(getString(R.string.emergency_sms_all_sent, successCount))
            } else if (successCount > 0) {
                val detail = results.joinToString("\n") {
                    if (it.success) {
                        getString(R.string.emergency_sms_sent_success, it.contact.name)
                    } else {
                        getString(R.string.emergency_sms_sent_failure, it.contact.name, it.errorMessage ?: "")
                    }
                }
                Alerts.dialog(
                    requireContext(),
                    getString(R.string.emergency_contacts_title),
                    detail
                )
            } else {
                toast(getString(R.string.emergency_sms_none_sent))
            }
        }
    }

    private fun showKvkkConsentDialog() {
        val dialogBinding = DialogKvkkBinding.inflate(layoutInflater)
        dialogBinding.kvkkCheckbox.isChecked = prefs.hasAcceptedKvkk

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .setPositiveButton(R.string.emergency_kvkk_approve_btn) { _, _ ->
                prefs.hasAcceptedKvkk = dialogBinding.kvkkCheckbox.isChecked
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                prefs.hasAcceptedKvkk = false
                dialog.dismiss()
            }
            .show()
    }

    private fun navigateToSettings() {
        findNavController().navigate(R.id.emergencySettingsFragment)
    }
}
