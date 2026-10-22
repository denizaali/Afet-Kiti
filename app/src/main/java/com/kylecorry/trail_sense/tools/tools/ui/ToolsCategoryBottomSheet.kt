package com.kylecorry.trail_sense.tools.tools.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import com.kylecorry.andromeda.fragments.BoundBottomSheetDialogFragment
import com.kylecorry.andromeda.list.GridView
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.databinding.FragmentToolCategoryBottomSheetBinding
import com.kylecorry.trail_sense.databinding.ListItemToolBinding
import com.kylecorry.trail_sense.tools.tools.infrastructure.Tool
import com.kylecorry.trail_sense.tools.tools.ui.items.ToolListItem
import com.kylecorry.trail_sense.tools.tools.ui.items.ToolListItemStyle
import com.kylecorry.trail_sense.tools.tools.ui.items.render.ToolButtonToolListItemRenderer

class ToolsCategoryBottomSheet(
    private val categoryName: String,
    @DrawableRes private val categoryIcon: Int,
    private val tools: List<Tool>,
    private val onToolClick: (Tool) -> Unit,
    private val onToolLongClick: (View, Tool) -> Unit
) : BoundBottomSheetDialogFragment<FragmentToolCategoryBottomSheetBinding>() {

    private val renderer = ToolButtonToolListItemRenderer()

    override fun generateBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentToolCategoryBottomSheetBinding {
        return FragmentToolCategoryBottomSheetBinding.inflate(layoutInflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.categoryTitle.text = categoryName
        binding.categoryIcon.setImageResource(categoryIcon)
        binding.categorySubtitle.text = resources.getQuantityString(
            R.plurals.num_tools,
            tools.size,
            tools.size
        )

        val gridView = GridView(
            binding.toolsList,
            R.layout.list_item_tool,
            2
        ) { itemView, toolItem: ToolListItem ->
            val itemBinding = ListItemToolBinding.bind(itemView)
            renderer.render(itemBinding, toolItem)
        }

        val items = tools.map { tool ->
            ToolListItem(
                title = tool.name,
                style = ToolListItemStyle.Tool,
                icon = tool.icon,
                onClick = {
                    dismiss()
                    onToolClick(tool)
                },
                onLongClick = { v ->
                    onToolLongClick(v, tool)
                    true
                }
            )
        }

        gridView.setData(items)
    }
}
