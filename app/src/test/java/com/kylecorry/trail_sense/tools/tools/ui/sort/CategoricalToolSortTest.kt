package com.kylecorry.trail_sense.tools.tools.ui.sort

import android.content.Context
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.tools.tools.infrastructure.Tool
import com.kylecorry.trail_sense.tools.tools.infrastructure.ToolCategory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class CategoricalToolSortTest {

    @Test
    fun sortsToolsByCategoryAndAssignsCategoryField() {
        val context: Context = mock()
        whenever(context.getString(R.string.tool_category_signaling)).thenReturn("Signaling")
        whenever(context.getString(R.string.distance)).thenReturn("Distance")

        val sorter = CategoricalToolSort(context)

        val tool1 = Tool(1, "Whistle", 0, 0, ToolCategory.Signaling)
        val tool2 = Tool(2, "Flashlight", 0, 0, ToolCategory.Signaling)
        val tool3 = Tool(3, "Ruler", 0, 0, ToolCategory.Distance)

        val result = sorter.sort(listOf(tool1, tool2, tool3))

        assertEquals(2, result.size)
        assertEquals(ToolCategory.Signaling, result[0].category)
        assertEquals("Signaling", result[0].categoryName)
        assertEquals(listOf("Flashlight", "Whistle"), result[0].tools.map { it.name })

        assertEquals(ToolCategory.Distance, result[1].category)
        assertEquals("Distance", result[1].categoryName)
        assertEquals(listOf("Ruler"), result[1].tools.map { it.name })
    }
}
