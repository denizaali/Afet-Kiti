package com.kylecorry.trail_sense.tools.emergency_call.ui

import android.Manifest
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kylecorry.andromeda.alerts.Alerts
import com.kylecorry.andromeda.fragments.BoundFragment
import com.kylecorry.andromeda.fragments.inBackground
import com.kylecorry.andromeda.permissions.Permissions
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.databinding.DialogEmergencyContactBinding
import com.kylecorry.trail_sense.databinding.DialogKvkkBinding
import com.kylecorry.trail_sense.databinding.FragmentEmergencySettingsBinding
import com.kylecorry.trail_sense.tools.emergency_call.domain.EmergencyContact
import com.kylecorry.trail_sense.tools.emergency_call.infrastructure.EmergencyPreferences
import com.kylecorry.trail_sense.tools.emergency_call.infrastructure.persistence.EmergencyContactRepo
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EmergencySettingsFragment : BoundFragment<FragmentEmergencySettingsBinding>() {

    private val prefs by lazy { EmergencyPreferences(requireContext()) }
    private val repo by lazy { EmergencyContactRepo.getInstance(requireContext()) }
    private lateinit var adapter: EmergencyContactAdapter

    override fun generateBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentEmergencySettingsBinding {
        return FragmentEmergencySettingsBinding.inflate(layoutInflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingsToolbar.leftButton.setOnClickListener {
            findNavController().navigateUp()
        }

        setupEmergencyNumberSelection()
        setupContactsList()
        setupSmsTemplateAndAutoSend()
        setupPermissionsAndKvkk()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
        updateKvkkStatus()
    }

    private fun setupEmergencyNumberSelection() {
        val currentNumber = prefs.emergencyNumber
        updateSelectedNumberPreview(currentNumber)

        when (currentNumber) {
            "112" -> binding.radio112.isChecked = true
            "122" -> binding.radio122.isChecked = true
            "155" -> binding.radio155.isChecked = true
            "156" -> binding.radio156.isChecked = true
            else -> binding.radioCustom.isChecked = true
        }

        binding.radioGroupEmergencyNumber.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_112 -> {
                    prefs.emergencyNumber = "112"
                    updateSelectedNumberPreview("112")
                }
                R.id.radio_122 -> {
                    prefs.emergencyNumber = "122"
                    updateSelectedNumberPreview("122")
                }
                R.id.radio_155 -> {
                    prefs.emergencyNumber = "155"
                    updateSelectedNumberPreview("155")
                }
                R.id.radio_156 -> {
                    prefs.emergencyNumber = "156"
                    updateSelectedNumberPreview("156")
                }
                R.id.radio_custom -> {
                    showCustomNumberDialog()
                }
            }
        }
    }

    private fun updateSelectedNumberPreview(number: String) {
        binding.selectedNumberPreview.text = getString(R.string.emergency_default_number_label, number)
    }

    private fun showCustomNumberDialog() {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_PHONE
            setText(prefs.emergencyNumber)
            setSelection(text.length)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.emergency_custom_number_dialog_title)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val newNumber = input.text.toString().trim()
                if (newNumber.isNotBlank()) {
                    prefs.emergencyNumber = newNumber
                    updateSelectedNumberPreview(newNumber)
                }
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                when (prefs.emergencyNumber) {
                    "112" -> binding.radio112.isChecked = true
                    "122" -> binding.radio122.isChecked = true
                    "155" -> binding.radio155.isChecked = true
                    "156" -> binding.radio156.isChecked = true
                    else -> binding.radioCustom.isChecked = true
                }
            }
            .show()
    }

    private fun setupContactsList() {
        adapter = EmergencyContactAdapter(
            onEdit = { contact -> showContactDialog(contact) },
            onDelete = { contact -> showDeleteContactDialog(contact) }
        )

        binding.emergencyContactsList.layoutManager = LinearLayoutManager(requireContext())
        binding.emergencyContactsList.adapter = adapter

        binding.btnAddContact.setOnClickListener {
            showContactDialog(null)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repo.getAll().collectLatest { contacts ->
                adapter.submitList(contacts)
                binding.emergencyContactsEmptyText.isVisible = contacts.isEmpty()
                binding.emergencyContactsList.isVisible = contacts.isNotEmpty()
            }
        }
    }

    private fun showContactDialog(existingContact: EmergencyContact?) {
        val dialogBinding = DialogEmergencyContactBinding.inflate(layoutInflater)
        if (existingContact != null) {
            dialogBinding.contactNameInput.setText(existingContact.name)
            dialogBinding.contactPhoneInput.setText(existingContact.phone)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existingContact == null) R.string.emergency_add_contact else R.string.emergency_edit_contact)
            .setView(dialogBinding.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val name = dialogBinding.contactNameInput.text.toString().trim()
                val phone = dialogBinding.contactPhoneInput.text.toString().trim()
                if (name.isNotBlank() && phone.isNotBlank()) {
                    inBackground {
                        val contactToSave = existingContact?.copy(name = name, phone = phone)
                            ?: EmergencyContact(name = name, phone = phone)
                        repo.add(contactToSave)
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showDeleteContactDialog(contact: EmergencyContact) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.emergency_delete_contact)
            .setMessage(getString(R.string.emergency_delete_contact_confirmation))
            .setPositiveButton(R.string.delete) { _, _ ->
                inBackground {
                    repo.delete(contact)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun setupSmsTemplateAndAutoSend() {
        binding.switchSendSmsOnCall.isChecked = prefs.sendSmsOnCall
        binding.switchSendSmsOnCall.setOnCheckedChangeListener { _, isChecked ->
            prefs.sendSmsOnCall = isChecked
        }

        binding.smsTemplateInput.setText(prefs.smsTemplate)
        binding.smsTemplateInput.addTextChangedListener {
            val text = it?.toString()
            if (!text.isNullOrBlank()) {
                prefs.smsTemplate = text
            }
        }

        binding.btnResetSmsTemplate.setOnClickListener {
            prefs.smsTemplate = EmergencyPreferences.DEFAULT_SMS_TEMPLATE
            binding.smsTemplateInput.setText(EmergencyPreferences.DEFAULT_SMS_TEMPLATE)
        }
    }

    private fun setupPermissionsAndKvkk() {
        binding.btnRequestLocationPermission.setOnClickListener {
            requestPermissions(
                listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ) {
                updatePermissionStatus()
            }
        }

        binding.btnRequestSmsPermission.setOnClickListener {
            requestPermissions(
                listOf(Manifest.permission.SEND_SMS)
            ) {
                updatePermissionStatus()
            }
        }

        binding.btnViewKvkk.setOnClickListener {
            showKvkkDialog()
        }

        updatePermissionStatus()
        updateKvkkStatus()
    }

    private fun updatePermissionStatus() {
        val hasLocation = Permissions.canGetFineLocation(requireContext())
        if (hasLocation) {
            binding.locationPermissionIcon.setImageResource(R.drawable.ic_check)
            binding.locationPermissionIcon.setColorFilter(resources.getColor(R.color.green, null))
            binding.locationPermissionText.setText(R.string.emergency_location_permission_granted)
            binding.btnRequestLocationPermission.isVisible = false
        } else {
            binding.locationPermissionIcon.setImageResource(R.drawable.ic_alert)
            binding.locationPermissionIcon.setColorFilter(resources.getColor(R.color.red, null))
            binding.locationPermissionText.setText(R.string.emergency_location_permission_denied)
            binding.btnRequestLocationPermission.isVisible = true
        }

        val hasSms = Permissions.hasPermission(requireContext(), Manifest.permission.SEND_SMS)
        if (hasSms) {
            binding.smsPermissionIcon.setImageResource(R.drawable.ic_check)
            binding.smsPermissionIcon.setColorFilter(resources.getColor(R.color.green, null))
            binding.smsPermissionText.setText(R.string.emergency_sms_permission_granted)
            binding.btnRequestSmsPermission.isVisible = false
        } else {
            binding.smsPermissionIcon.setImageResource(R.drawable.ic_alert)
            binding.smsPermissionIcon.setColorFilter(resources.getColor(R.color.red, null))
            binding.smsPermissionText.setText(R.string.emergency_sms_permission_denied)
            binding.btnRequestSmsPermission.isVisible = true
        }
    }

    private fun updateKvkkStatus() {
        val accepted = prefs.hasAcceptedKvkk
        if (accepted) {
            binding.kvkkStatusIcon.setImageResource(R.drawable.ic_check)
            binding.kvkkStatusIcon.setColorFilter(resources.getColor(R.color.green, null))
            binding.kvkkStatusText.setText(R.string.emergency_kvkk_accepted)
        } else {
            binding.kvkkStatusIcon.setImageResource(R.drawable.ic_alert)
            binding.kvkkStatusIcon.setColorFilter(resources.getColor(R.color.red, null))
            binding.kvkkStatusText.setText(R.string.emergency_kvkk_not_accepted)
        }
    }

    private fun showKvkkDialog() {
        val dialogBinding = DialogKvkkBinding.inflate(layoutInflater)
        dialogBinding.kvkkCheckbox.isChecked = prefs.hasAcceptedKvkk

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.emergency_kvkk_approve_btn) { _, _ ->
                prefs.hasAcceptedKvkk = dialogBinding.kvkkCheckbox.isChecked
                updateKvkkStatus()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
