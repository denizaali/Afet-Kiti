package com.kylecorry.trail_sense.tools.counter.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.kylecorry.andromeda.alerts.Alerts
import com.kylecorry.andromeda.fragments.BoundFragment
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.databinding.FragmentToolCounterBinding
import com.kylecorry.trail_sense.databinding.ItemCounterBinding
import com.kylecorry.trail_sense.databinding.ItemCounterSuggestionBinding
import com.kylecorry.trail_sense.tools.counter.domain.Counter

class ToolCounterFragment : BoundFragment<FragmentToolCounterBinding>() {

    private var counters = emptyList<Counter>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.counterAddBtn.setOnClickListener { askName() }
        showSuggestions()
        show()
    }

    private fun show() {
        binding.counterList.removeAllViews()
        for (counter in counters) {
            val row = ItemCounterBinding.inflate(layoutInflater, binding.counterList, false)
            row.counterName.text = counter.name
            row.counterValue.text = counter.count.toString()
            row.counterPlusBtn.setOnClickListener { change(counter.id, +1) }
            row.counterMinusBtn.setOnClickListener { change(counter.id, -1) }
            row.counterDeleteBtn.setOnClickListener { confirmDelete(counter) }
            binding.counterList.addView(row.root)
        }
        binding.counterEmptyView.isVisible = counters.isEmpty()
    }

    private fun showSuggestions() {
        for (nameId in CounterSuggestions.nameIds) {
            val chip = ItemCounterSuggestionBinding.inflate(
                layoutInflater, binding.counterSuggestions, false
            ).root
            chip.text = getString(nameId)
            chip.setOnClickListener { add(chip.text.toString()) }
            binding.counterSuggestions.addView(chip)
        }
    }

    private fun change(id: Long, amount: Int) {
        counters = counters.map {
            if (it.id == id) it.copy(count = maxOf(0, it.count + amount)) else it
        }
        show()
    }

    private fun askName() {
        val input = EditText(requireContext())
        input.hint = "Örn. El feneri"

        AlertDialog.Builder(requireContext())
            .setTitle("Yeni sayaç")
            .setView(input)
            .setPositiveButton("Ekle") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    add(name)
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun add(name: String) {
        val newId = (counters.maxOfOrNull { it.id } ?: 0) + 1
        counters = counters + Counter(id = newId, name = name, count = 0)
        show()
    }

    private fun confirmDelete(counter: Counter) {
        Alerts.dialog(
            requireContext(),
            getString(R.string.delete),
            counter.name
        ) { cancelled ->
            if (!cancelled) {
                delete(counter.id)
            }
        }
    }

    private fun delete(id: Long) {
        counters = counters.filterNot { it.id == id }
        show()
    }

    override fun generateBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentToolCounterBinding {
        return FragmentToolCounterBinding.inflate(layoutInflater, container, false)
    }
}
