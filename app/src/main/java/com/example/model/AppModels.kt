package com.example.model

data class AppWindowRule(
    val packageName: String,
    val appName: String,
    val presetId: String,
    val isEnabled: Boolean = true
)

data class InstalledApp(
    val packageName: String,
    val appName: String,
    val launchIntentAvailable: Boolean = true
)

enum class FloatingToolType(val titleResId: Int) {
    CALCULATOR(com.example.R.string.mini_calc_title),
    NOTES(com.example.R.string.mini_notes_title),
    STOPWATCH(com.example.R.string.mini_timer_title),
    BROWSER(com.example.R.string.mini_browser_title)
}
