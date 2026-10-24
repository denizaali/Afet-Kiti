package com.kylecorry.trail_sense.tools.counter.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kylecorry.andromeda.fragments.BoundFragment
import com.kylecorry.trail_sense.databinding.FragmentToolCounterBinding
import com.kylecorry.trail_sense.databinding.ItemCounterBinding
import com.kylecorry.trail_sense.tools.counter.domain.Counter

class ToolCounterFragment : BoundFragment<FragmentToolCounterBinding>() {

    private var counters = listOf(
        Counter(id = 1, name = "Su şişesi", count = 0),
        Counter(id = 2, name = "Battaniye", count = 0)
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            binding.counterList.addView(row.root)
        }
    }

    private fun change(id: Long, amount: Int) {
        counters = counters.map {
            if (it.id == id) it.copy(count = maxOf(0, it.count + amount)) else it
        }
        show()
    }

    override fun generateBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentToolCounterBinding {
        return FragmentToolCounterBinding.inflate(layoutInflater, container, false)
    }
}
