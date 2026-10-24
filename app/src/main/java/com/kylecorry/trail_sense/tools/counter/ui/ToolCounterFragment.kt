package com.kylecorry.trail_sense.tools.counter.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kylecorry.andromeda.fragments.BoundFragment
import com.kylecorry.trail_sense.databinding.FragmentToolCounterBinding
import com.kylecorry.trail_sense.tools.counter.domain.Counter

class ToolCounterFragment : BoundFragment<FragmentToolCounterBinding>() {

    private var counter = Counter(id = 1, name = "Su şişesi", count = 0)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.counterPlusBtn.setOnClickListener {
            counter = counter.copy(count = counter.count + 1)
            show()
        }
        binding.counterMinusBtn.setOnClickListener {
            if (counter.count > 0) {
                counter = counter.copy(count = counter.count - 1)
            }
            show()
        }
        show()
    }

    private fun show() {
        binding.counterValue.text = counter.count.toString()
    }

    override fun generateBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentToolCounterBinding {
        return FragmentToolCounterBinding.inflate(layoutInflater, container, false)
    }
}
