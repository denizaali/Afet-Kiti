package com.kylecorry.trail_sense.settings.ui

import android.content.Context
import com.kylecorry.trail_sense.shared.UserPreferences
import com.kylecorry.trail_sense.tools.tools.infrastructure.Tool
import com.kylecorry.trail_sense.tools.tools.infrastructure.Tools

class BottomNavigationToolManager(
    private val prefs: UserPreferences,
    private val context: Context
) {

    fun getSelectedToolIds(): List<Long> {
        return prefs.bottomNavigationTools
    }

    fun getDefaultToolIds(): List<Long> {
        return prefs.defaultBottomNavigationTools
    }

    fun getTools(toolIds: List<Long> = getSelectedToolIds()): List<Tool?> {
        val allTools = Tools.getTools(context)
        return toolIds.map { toolId ->
            allTools.firstOrNull { it.id == toolId }
        }
    }

    fun getSelectedTools(): List<Tool?> {
        return getTools(getSelectedToolIds())
    }

    fun getAvailableToolsForSelection(selectedIds: Collection<Long> = getSelectedToolIds()): List<Tool> {
        val selectedSet = selectedIds.toSet()
        return Tools.getTools(context)
            .filter { !selectedSet.contains(it.id) }
            .sortedBy { it.name.lowercase() }
    }

    fun swap(items: MutableList<Long>, fromPosition: Int, toPosition: Int): List<Long> {
        if (fromPosition in items.indices && toPosition in items.indices && fromPosition != toPosition) {
            val temp = items[fromPosition]
            items[fromPosition] = items[toPosition]
            items[toPosition] = temp
        }
        return items
    }

    fun moveUp(items: MutableList<Long>, position: Int): List<Long> {
        if (position > 0) {
            return swap(items, position, position - 1)
        }
        return items
    }

    fun moveDown(items: MutableList<Long>, position: Int): List<Long> {
        if (position < 3) {
            return swap(items, position, position + 1)
        }
        return items
    }

    fun replace(items: MutableList<Long>, position: Int, newToolId: Long): List<Long> {
        if (position in items.indices) {
            val existingIndex = items.indexOf(newToolId)
            if (existingIndex != -1 && existingIndex != position) {
                // If the tool is already selected in another slot, swap them
                val temp = items[position]
                items[position] = newToolId
                items[existingIndex] = temp
            } else {
                items[position] = newToolId
            }
        }
        return items
    }

    fun save(items: List<Long>) {
        prefs.bottomNavigationTools = items
    }

    fun swap(fromPosition: Int, toPosition: Int): List<Long> {
        val items = getSelectedToolIds().toMutableList()
        swap(items, fromPosition, toPosition)
        save(items)
        return items
    }

    fun moveUp(position: Int): List<Long> {
        val items = getSelectedToolIds().toMutableList()
        moveUp(items, position)
        save(items)
        return items
    }

    fun moveDown(position: Int): List<Long> {
        val items = getSelectedToolIds().toMutableList()
        moveDown(items, position)
        save(items)
        return items
    }

    fun replace(position: Int, newToolId: Long): List<Long> {
        val items = getSelectedToolIds().toMutableList()
        replace(items, position, newToolId)
        save(items)
        return items
    }

    fun reset(): List<Long> {
        prefs.resetBottomNavigationTools()
        return prefs.bottomNavigationTools
    }
}
