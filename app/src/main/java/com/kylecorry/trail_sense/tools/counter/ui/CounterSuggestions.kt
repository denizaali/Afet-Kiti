package com.kylecorry.trail_sense.tools.counter.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kylecorry.trail_sense.R

/**
 * Önerilen bir afet çantası malzemesi: adı ve ikonu.
 */
data class CounterSuggestion(
    @StringRes val nameId: Int,
    @DrawableRes val iconId: Int
)

/**
 * Sayaç ekranında gösterilen afet çantası önerileri.
 * Liste AFAD'ın afet ve acil durum çantası önerilerinden seçildi.
 */
object CounterSuggestions {
    val items = listOf(
        CounterSuggestion(R.string.counter_suggestion_water, R.drawable.ic_category_water),
        CounterSuggestion(R.string.counter_suggestion_canned_food, R.drawable.ic_category_food),
        CounterSuggestion(R.string.counter_suggestion_flashlight, R.drawable.flashlight),
        CounterSuggestion(R.string.counter_suggestion_batteries, R.drawable.ic_tool_battery),
        CounterSuggestion(R.string.counter_suggestion_radio, R.drawable.cell_tower),
        CounterSuggestion(R.string.counter_suggestion_first_aid, R.drawable.ic_category_medical),
        CounterSuggestion(R.string.counter_suggestion_medicine, R.drawable.ic_counter_medicine),
        CounterSuggestion(R.string.counter_suggestion_blanket, R.drawable.ic_category_shelter),
        CounterSuggestion(R.string.counter_suggestion_mask, R.drawable.ic_counter_mask),
        CounterSuggestion(R.string.counter_suggestion_wet_wipes, R.drawable.ic_hygiene)
    )
}
