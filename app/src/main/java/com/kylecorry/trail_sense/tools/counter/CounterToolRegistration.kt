package com.kylecorry.trail_sense.tools.counter

import android.content.Context
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.tools.tools.infrastructure.Tool
import com.kylecorry.trail_sense.tools.tools.infrastructure.ToolCategory
import com.kylecorry.trail_sense.tools.tools.infrastructure.ToolRegistration
import com.kylecorry.trail_sense.tools.tools.infrastructure.Tools

object CounterToolRegistration : ToolRegistration {
    override fun getTool(context: Context): Tool {
        return Tool(
            Tools.COUNTER,                                  // kimlik numarası (Tools.kt'de tanımlı)
            context.getString(R.string.tool_counter_title), // ekranda görünen isim
            R.drawable.ic_tool_counter,                     // ikon (çetele)
            R.id.toolCounterFragment,                       // tıklayınca açılacak ekran
            ToolCategory.Other                              // Tools listesinde hangi başlık altında
        )
    }
}
