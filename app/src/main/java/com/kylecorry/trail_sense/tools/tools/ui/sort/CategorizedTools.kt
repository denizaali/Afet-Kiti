package com.kylecorry.trail_sense.tools.tools.ui.sort

import com.kylecorry.trail_sense.tools.tools.infrastructure.Tool
import com.kylecorry.trail_sense.tools.tools.infrastructure.ToolCategory

data class CategorizedTools(
    val categoryName: String?,
    val tools: List<Tool>,
    val category: ToolCategory? = null
)
