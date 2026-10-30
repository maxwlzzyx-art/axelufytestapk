package com.example

import com.example.model.WindowPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WindowPresetTest {

    @Test
    fun defaultPresets_areValid() {
        val presets = WindowPreset.DEFAULT_PRESETS
        assertTrue(presets.isNotEmpty())
        val defaultPreset = presets.firstOrNull { it.isDefault }
        assertNotNull(defaultPreset)
        assertEquals("preset_standard", defaultPreset?.id)
    }

    @Test
    fun presetDimensions_withinAllowedRange() {
        WindowPreset.DEFAULT_PRESETS.forEach { preset ->
            assertTrue("Width should be between 20 and 100", preset.widthPercent in 20..100)
            assertTrue("Height should be between 20 and 100", preset.heightPercent in 20..100)
            assertTrue("Position X should be between 0 and 100", preset.positionXPercent in 0..100)
            assertTrue("Position Y should be between 0 and 100", preset.positionYPercent in 0..100)
            assertTrue("Corner radius should be >= 4", preset.cornerRadiusDp >= 4)
        }
    }
}
