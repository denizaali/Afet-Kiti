package com.kylecorry.trail_sense.tools.counter.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kylecorry.andromeda.fragments.BoundFragment
import com.kylecorry.trail_sense.databinding.FragmentToolCounterBinding

class ToolCounterFragment : BoundFragment<FragmentToolCounterBinding>() {

    private var count = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.counterPlusBtn.setOnClickListener {
            count++
            show()
        }
        binding.counterMinusBtn.setOnClickListener {
            if (count > 0) count--
            show()
        }
        show()
    }

    private fun show() {
        binding.counterValue.text = count.toString()
    }

    override fun generateBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentToolCounterBinding {
        return FragmentToolCounterBinding.inflate(layoutInflater, container, false)
    }
}
