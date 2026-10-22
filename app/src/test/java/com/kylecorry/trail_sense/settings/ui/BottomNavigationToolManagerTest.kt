package com.kylecorry.trail_sense.settings.ui

import android.content.Context
import com.kylecorry.trail_sense.shared.UserPreferences
import com.kylecorry.trail_sense.tools.tools.infrastructure.Tools
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class BottomNavigationToolManagerTest {

    private lateinit var prefs: UserPreferences
    private lateinit var context: Context
    private lateinit var manager: BottomNavigationToolManager

    @BeforeEach
    fun setup() {
        prefs = mock()
        context = mock()
        whenever(prefs.bottomNavigationTools).thenReturn(
            listOf(Tools.NAVIGATION, Tools.MAP, Tools.WEATHER, Tools.ASTRONOMY)
        )
        manager = BottomNavigationToolManager(prefs, context)
    }

    @Test
    fun getsSelectedToolIds() {
        assertEquals(
            listOf(Tools.NAVIGATION, Tools.MAP, Tools.WEATHER, Tools.ASTRONOMY),
            manager.getSelectedToolIds()
        )
    }

    @Test
    fun inMemorySwapWorks() {
        val list = mutableListOf(Tools.NAVIGATION, Tools.MAP, Tools.WEATHER, Tools.ASTRONOMY)
        manager.swap(list, 0, 2)
        assertEquals(
            listOf(Tools.WEATHER, Tools.MAP, Tools.NAVIGATION, Tools.ASTRONOMY),
            list
        )
    }

    @Test
    fun inMemoryMoveUpAndDownWorks() {
        val list = mutableListOf(Tools.NAVIGATION, Tools.MAP, Tools.WEATHER, Tools.ASTRONOMY)
        manager.moveUp(list, 1)
        assertEquals(
            listOf(Tools.MAP, Tools.NAVIGATION, Tools.WEATHER, Tools.ASTRONOMY),
            list
        )
        manager.moveDown(list, 0)
        assertEquals(
            listOf(Tools.NAVIGATION, Tools.MAP, Tools.WEATHER, Tools.ASTRONOMY),
            list
        )
    }

    @Test
    fun inMemoryReplaceWorks() {
        val list = mutableListOf(Tools.NAVIGATION, Tools.MAP, Tools.WEATHER, Tools.ASTRONOMY)
        manager.replace(list, 1, Tools.PEDOMETER)
        assertEquals(
            listOf(Tools.NAVIGATION, Tools.PEDOMETER, Tools.WEATHER, Tools.ASTRONOMY),
            list
        )
        // Swaps if already present
        manager.replace(list, 0, Tools.WEATHER)
        assertEquals(
            listOf(Tools.WEATHER, Tools.PEDOMETER, Tools.NAVIGATION, Tools.ASTRONOMY),
            list
        )
    }

    @Test
    fun savesListToPreferences() {
        val list = listOf(Tools.WEATHER, Tools.PEDOMETER, Tools.NAVIGATION, Tools.ASTRONOMY)
        manager.save(list)
        verify(prefs).bottomNavigationTools = list
    }

    @Test
    fun swapsToolsCorrectly() {
        manager.swap(0, 2)
        verify(prefs).bottomNavigationTools = listOf(
            Tools.WEATHER,
            Tools.MAP,
            Tools.NAVIGATION,
            Tools.ASTRONOMY
        )
    }

    @Test
    fun movesToolUp() {
        manager.moveUp(1)
        verify(prefs).bottomNavigationTools = listOf(
            Tools.MAP,
            Tools.NAVIGATION,
            Tools.WEATHER,
            Tools.ASTRONOMY
        )
    }

    @Test
    fun movesToolDown() {
        manager.moveDown(0)
        verify(prefs).bottomNavigationTools = listOf(
            Tools.MAP,
            Tools.NAVIGATION,
            Tools.WEATHER,
            Tools.ASTRONOMY
        )
    }

    @Test
    fun replacesToolWithNewTool() {
        val newToolId = Tools.PEDOMETER
        manager.replace(1, newToolId)
        verify(prefs).bottomNavigationTools = listOf(
            Tools.NAVIGATION,
            Tools.PEDOMETER,
            Tools.WEATHER,
            Tools.ASTRONOMY
        )
    }

    @Test
    fun replacesWithExistingToolPerformsSwapToAvoidDuplicates() {
        // If Tools.WEATHER (currently at index 2) is chosen for slot 0, they should swap
        manager.replace(0, Tools.WEATHER)
        verify(prefs).bottomNavigationTools = listOf(
            Tools.WEATHER,
            Tools.MAP,
            Tools.NAVIGATION,
            Tools.ASTRONOMY
        )
    }

    @Test
    fun resetsToDefaults() {
        manager.reset()
        verify(prefs).resetBottomNavigationTools()
    }
}
