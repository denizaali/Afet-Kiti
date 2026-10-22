package com.kylecorry.trail_sense.settings.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.kylecorry.andromeda.alerts.Alerts
import com.kylecorry.andromeda.fragments.BoundFragment
import com.kylecorry.andromeda.pickers.Pickers
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.databinding.FragmentBottomNavigationSettingsBinding
import com.kylecorry.trail_sense.databinding.ListItemBottomNavSlotBinding
import com.kylecorry.trail_sense.shared.UserPreferences
import com.kylecorry.trail_sense.shared.requireMainActivity
import com.kylecorry.trail_sense.tools.tools.infrastructure.Tool
import java.util.Collections

class BottomNavigationSettingsFragment :
    BoundFragment<FragmentBottomNavigationSettingsBinding>() {

    private val prefs by lazy { UserPreferences(requireContext()) }
    private val manager by lazy { BottomNavigationToolManager(prefs, requireContext()) }
    private lateinit var adapter: BottomNavSlotAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    private var draftToolIds = mutableListOf<Long>()

    override fun generateBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBottomNavigationSettingsBinding {
        return FragmentBottomNavigationSettingsBinding.inflate(layoutInflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        draftToolIds = manager.getSelectedToolIds().toMutableList()

        binding.toolbar.rightButton.contentDescription = getString(R.string.save)
        binding.toolbar.rightButton.setOnClickListener {
            saveChanges()
        }

        binding.btnResetDefaults.setOnClickListener {
            confirmReset()
        }

        setupRecyclerView()
        updateDraftUI()
    }

    private fun setupRecyclerView() {
        adapter = BottomNavSlotAdapter(
            items = manager.getTools(draftToolIds).toMutableList(),
            onStartDrag = { holder ->
                itemTouchHelper.startDrag(holder)
            },
            onMoveUp = { position ->
                manager.moveUp(draftToolIds, position)
                updateDraftUI()
            },
            onMoveDown = { position ->
                manager.moveDown(draftToolIds, position)
                updateDraftUI()
            },
            onChange = { position ->
                showToolPicker(position)
            }
        )

        itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.bindingAdapterPosition
                val to = target.bindingAdapterPosition
                if (from in 0..3 && to in 0..3 && from != to) {
                    manager.swap(draftToolIds, from, to)
                    adapter.moveItem(from, to)
                    updatePreview()
                    return true
                }
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Swiping is not supported
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                // Refresh item positions/badges and up/down buttons once drag ends
                adapter.updateItems(manager.getTools(draftToolIds))
            }

            override fun isLongPressDragEnabled(): Boolean = false
        })

        binding.customSlotsList.adapter = adapter
        itemTouchHelper.attachToRecyclerView(binding.customSlotsList)
    }

    private fun showToolPicker(position: Int) {
        val availableTools = manager.getAvailableToolsForSelection(draftToolIds)
        if (availableTools.isEmpty()) {
            return
        }

        val names = availableTools.map { it.name }
        val title = getString(R.string.bottom_nav_replace_title, position + 1)

        Pickers.item(
            requireContext(),
            title,
            names,
            -1
        ) { selectedIndex ->
            if (selectedIndex != null && selectedIndex in availableTools.indices) {
                val selectedTool = availableTools[selectedIndex]
                manager.replace(draftToolIds, position, selectedTool.id)
                updateDraftUI()
            }
        }
    }

    private fun confirmReset() {
        Alerts.dialog(
            requireContext(),
            getString(R.string.bottom_nav_reset_to_default),
            getString(R.string.bottom_nav_reset_confirm)
        ) { cancelled ->
            if (!cancelled) {
                draftToolIds = manager.getDefaultToolIds().toMutableList()
                updateDraftUI()
            }
        }
    }

    private fun saveChanges() {
        manager.save(draftToolIds)
        requireMainActivity().updateBottomNavigation()
        Alerts.toast(requireContext(), getString(R.string.saved))
    }

    private fun updateDraftUI() {
        adapter.updateItems(manager.getTools(draftToolIds))
        updatePreview()
    }

    private fun updatePreview() {
        val selectedTools = manager.getTools(draftToolIds)
        val previewSlots = listOf(
            Pair(binding.previewIcon1, binding.previewName1),
            Pair(binding.previewIcon2, binding.previewName2),
            Pair(binding.previewIcon3, binding.previewName3),
            Pair(binding.previewIcon4, binding.previewName4)
        )

        previewSlots.forEachIndexed { index, (iconView, nameView) ->
            val tool = selectedTools.getOrNull(index)
            if (tool != null) {
                iconView.setImageResource(tool.icon)
                nameView.text = tool.name
            } else {
                iconView.setImageResource(R.drawable.apps)
                nameView.text = getString(R.string.none)
            }
        }
    }

    class BottomNavSlotAdapter(
        private var items: MutableList<Tool?>,
        private val onStartDrag: (RecyclerView.ViewHolder) -> Unit,
        private val onMoveUp: (Int) -> Unit,
        private val onMoveDown: (Int) -> Unit,
        private val onChange: (Int) -> Unit
    ) : RecyclerView.Adapter<BottomNavSlotAdapter.SlotViewHolder>() {

        @SuppressLint("NotifyDataSetChanged")
        fun updateItems(newItems: List<Tool?>) {
            items = newItems.toMutableList()
            notifyDataSetChanged()
        }

        fun moveItem(from: Int, to: Int) {
            if (from in items.indices && to in items.indices) {
                Collections.swap(items, from, to)
                notifyItemMoved(from, to)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
            val binding = ListItemBottomNavSlotBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return SlotViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
            holder.bind(items.getOrNull(position), position)
        }

        override fun getItemCount(): Int = items.size

        inner class SlotViewHolder(val binding: ListItemBottomNavSlotBinding) :
            RecyclerView.ViewHolder(binding.root) {

            @SuppressLint("ClickableViewAccessibility")
            fun bind(tool: Tool?, position: Int) {
                binding.slotBadge.text = "${position + 1}."

                if (tool != null) {
                    binding.toolIcon.setImageResource(tool.icon)
                    binding.toolName.text = tool.name
                    binding.toolCategory.text = tool.category.name
                } else {
                    binding.toolIcon.setImageResource(R.drawable.apps)
                    binding.toolName.text = binding.root.context.getString(R.string.none)
                    binding.toolCategory.text = ""
                }

                binding.btnMoveUp.isVisible = position > 0
                binding.btnMoveDown.isVisible = position < 3

                binding.btnMoveUp.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos in 1..3) {
                        onMoveUp(pos)
                    }
                }

                binding.btnMoveDown.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos in 0..2) {
                        onMoveDown(pos)
                    }
                }

                val openPicker = {
                    val pos = bindingAdapterPosition
                    if (pos in 0..3) {
                        onChange(pos)
                    }
                }
                binding.btnChange.setOnClickListener { openPicker() }
                binding.textContainer.setOnClickListener { openPicker() }
                binding.toolIcon.setOnClickListener { openPicker() }

                binding.dragHandle.setOnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        onStartDrag(this)
                    }
                    false
                }
            }
        }
    }
}
