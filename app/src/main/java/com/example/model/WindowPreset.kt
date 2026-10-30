package com.example.model

data class WindowPreset(
    val id: String,
    val name: String,
    val widthPercent: Int,      // 20% to 100% of screen width
    val heightPercent: Int,     // 20% to 100% of screen height
    val positionXPercent: Int,  // 0% (Left) to 100% (Right)
    val positionYPercent: Int,  // 0% (Top) to 100% (Bottom)
    val cornerRadiusDp: Int = 16,
    val opacityPercent: Int = 100, // 50% to 100%
    val snapEdges: Boolean = true,
    val isDefault: Boolean = false,
    val isBuiltIn: Boolean = false
) {
    companion object {
        val DEFAULT_PRESETS = listOf(
            WindowPreset(
                id = "preset_standard",
                name = "Standard Float",
                widthPercent = 65,
                heightPercent = 60,
                positionXPercent = 50,
                positionYPercent = 40,
                cornerRadiusDp = 20,
                opacityPercent = 100,
                isDefault = true,
                isBuiltIn = true
            ),
            WindowPreset(
                id = "preset_compact",
                name = "Compact Mini",
                widthPercent = 45,
                heightPercent = 40,
                positionXPercent = 90,
                positionYPercent = 20,
                cornerRadiusDp = 18,
                opacityPercent = 95,
                isBuiltIn = true
            ),
            WindowPreset(
                id = "preset_half_left",
                name = "Split Left",
                widthPercent = 50,
                heightPercent = 96,
                positionXPercent = 0,
                positionYPercent = 50,
                cornerRadiusDp = 12,
                isBuiltIn = true
            ),
            WindowPreset(
                id = "preset_half_right",
                name = "Split Right",
                widthPercent = 50,
                heightPercent = 96,
                positionXPercent = 100,
                positionYPercent = 50,
                cornerRadiusDp = 12,
                isBuiltIn = true
            ),
            WindowPreset(
                id = "preset_landscape",
                name = "16:9 Cinema",
                widthPercent = 80,
                heightPercent = 45,
                positionXPercent = 50,
                positionYPercent = 30,
                cornerRadiusDp = 16,
                isBuiltIn = true
            ),
            WindowPreset(
                id = "preset_desktop",
                name = "Desktop Square",
                widthPercent = 70,
                heightPercent = 70,
                positionXPercent = 50,
                positionYPercent = 50,
                cornerRadiusDp = 20,
                isBuiltIn = true
            )
        )
    }
}
