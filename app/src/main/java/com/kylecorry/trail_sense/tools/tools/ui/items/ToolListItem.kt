package com.kylecorry.trail_sense.tools.tools.ui.items

import android.view.View

enum class ToolListItemStyle {
    Header,
    Category,
    CategoryCard,
    Tool
}

data class ToolListItem(
    val title: String?,
    val style: ToolListItemStyle,
    val icon: Int? = null,
    val subtitle: String? = null,
    val onClick: (view: View) -> Unit = {},
    val onLongClick: (view: View) -> Boolean = { false }
)
