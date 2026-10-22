package com.kylecorry.trail_sense.tools.emergency_call.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kylecorry.trail_sense.databinding.ListItemEmergencyContactBinding
import com.kylecorry.trail_sense.tools.emergency_call.domain.EmergencyContact

class EmergencyContactAdapter(
    private val onEdit: (EmergencyContact) -> Unit,
    private val onDelete: (EmergencyContact) -> Unit
) : ListAdapter<EmergencyContact, EmergencyContactAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemEmergencyContactBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ListItemEmergencyContactBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: EmergencyContact) {
            binding.contactName.text = contact.name
            binding.contactPhone.text = contact.phone

            binding.btnEditContact.setOnClickListener {
                onEdit(contact)
            }

            binding.btnDeleteContact.setOnClickListener {
                onDelete(contact)
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<EmergencyContact>() {
        override fun areItemsTheSame(oldItem: EmergencyContact, newItem: EmergencyContact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EmergencyContact, newItem: EmergencyContact): Boolean {
            return oldItem == newItem
        }
    }
}
