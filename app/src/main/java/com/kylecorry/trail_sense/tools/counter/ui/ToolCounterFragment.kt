package com.kylecorry.trail_sense.tools.counter.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.kylecorry.andromeda.alerts.Alerts
import com.kylecorry.andromeda.fragments.BoundFragment
import com.kylecorry.andromeda.pickers.Pickers
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.databinding.FragmentToolCounterBinding
import com.kylecorry.trail_sense.databinding.ItemCounterBinding
import com.kylecorry.trail_sense.databinding.ItemCounterSuggestionBinding
import com.kylecorry.trail_sense.tools.counter.domain.Counter

class ToolCounterFragment : BoundFragment<FragmentToolCounterBinding>() {

    private var counters = emptyList<Counter>()
    private var shownSuggestions = emptyList<CounterSuggestion>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.counterAddBtn.setOnClickListener { askName() }
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
        updateSuggestions()
    }

    private fun updateSuggestions() {
        val remaining = CounterSuggestions.items.filterNot {
            hasCounterNamed(getString(it.nameId))
        }
        binding.counterSuggestionsTitle.isVisible = remaining.isNotEmpty()
        binding.counterSuggestions.isVisible = remaining.isNotEmpty()

        // Kartları sadece liste değişince yeniden çiz (+/- basışlarında değil)
        if (remaining != shownSuggestions) {
            shownSuggestions = remaining
            binding.counterSuggestions.removeAllViews()
            remaining.forEach { binding.counterSuggestions.addView(createSuggestionCard(it)) }
        }
    }

    private fun createSuggestionCard(suggestion: CounterSuggestion): View {
        val card = ItemCounterSuggestionBinding.inflate(
            layoutInflater, binding.counterSuggestions, false
        )
        val name = getString(suggestion.nameId)
        card.suggestionName.text = name
        card.suggestionIcon.setImageResource(suggestion.iconId)
        card.root.setOnClickListener { add(name) }
        return card.root
    }

    private fun hasCounterNamed(name: String): Boolean {
        return counters.any { it.name.equals(name, ignoreCase = true) }
    }

    private fun change(id: Long, amount: Int) {
        counters = counters.map {
            if (it.id == id) it.copy(count = maxOf(0, it.count + amount)) else it
        }
        show()
    }

    private fun askName() {
        Pickers.text(
            requireContext(),
            getString(R.string.counter_new_title),
            null,
            null,
            getString(R.string.counter_name_hint)
        ) { text ->
            val name = text?.trim()
            if (!name.isNullOrEmpty()) {
                add(name)
            }
        }
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
