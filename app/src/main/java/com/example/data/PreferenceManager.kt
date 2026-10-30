package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppWindowRule
import com.example.model.WindowPreset
import org.json.JSONArray
import org.json.JSONObject

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("mi_freeform_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PRESETS = "key_presets"
        private const val KEY_APP_RULES = "key_app_rules"
        private const val KEY_DEFAULT_PRESET_ID = "key_default_preset_id"
        private const val KEY_DOCK_ENABLED = "key_dock_enabled"
        private const val KEY_DOCK_SIDE_RIGHT = "key_dock_side_right"
        private const val KEY_DOCK_OPACITY = "key_dock_opacity"
        private const val KEY_DOCK_AUTO_HIDE = "key_dock_auto_hide"
        private const val KEY_HAPTIC_FEEDBACK = "key_haptic_feedback"
        private const val KEY_MULTIPLE_TASK_FLAGS = "key_multiple_task_flags"
        private const val KEY_SHIZUKU_MODE = "key_shizuku_mode"
    }

    fun getPresets(): List<WindowPreset> {
        val rawJson = prefs.getString(KEY_PRESETS, null)
        if (rawJson.isNullOrEmpty()) {
            savePresets(WindowPreset.DEFAULT_PRESETS)
            return WindowPreset.DEFAULT_PRESETS
        }

        return try {
            val jsonArray = JSONArray(rawJson)
            val list = mutableListOf<WindowPreset>()
            val defaultId = getDefaultPresetId()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                list.add(
                    WindowPreset(
                        id = id,
                        name = obj.getString("name"),
                        widthPercent = obj.optInt("widthPercent", 60),
                        heightPercent = obj.optInt("heightPercent", 60),
                        positionXPercent = obj.optInt("positionXPercent", 50),
                        positionYPercent = obj.optInt("positionYPercent", 50),
                        cornerRadiusDp = obj.optInt("cornerRadiusDp", 16),
                        opacityPercent = obj.optInt("opacityPercent", 100),
                        snapEdges = obj.optBoolean("snapEdges", true),
                        isDefault = id == defaultId,
                        isBuiltIn = obj.optBoolean("isBuiltIn", false)
                    )
                )
            }
            if (list.isEmpty()) WindowPreset.DEFAULT_PRESETS else list
        } catch (e: Exception) {
            WindowPreset.DEFAULT_PRESETS
        }
    }

    fun savePresets(presets: List<WindowPreset>) {
        val jsonArray = JSONArray()
        for (p in presets) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("widthPercent", p.widthPercent)
                put("heightPercent", p.heightPercent)
                put("positionXPercent", p.positionXPercent)
                put("positionYPercent", p.positionYPercent)
                put("cornerRadiusDp", p.cornerRadiusDp)
                put("opacityPercent", p.opacityPercent)
                put("snapEdges", p.snapEdges)
                put("isBuiltIn", p.isBuiltIn)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_PRESETS, jsonArray.toString()).apply()
    }

    fun savePreset(preset: WindowPreset) {
        val current = getPresets().toMutableList()
        val index = current.indexOfFirst { it.id == preset.id }
        if (index >= 0) {
            current[index] = preset
        } else {
            current.add(preset)
        }
        savePresets(current)
    }

    fun deletePreset(presetId: String) {
        val current = getPresets().filterNot { it.id == presetId }
        savePresets(current)
        if (getDefaultPresetId() == presetId && current.isNotEmpty()) {
            setDefaultPresetId(current.first().id)
        }
    }

    fun getDefaultPresetId(): String {
        return prefs.getString(KEY_DEFAULT_PRESET_ID, "preset_standard") ?: "preset_standard"
    }

    fun setDefaultPresetId(id: String) {
        prefs.edit().putString(KEY_DEFAULT_PRESET_ID, id).apply()
    }

    fun getDefaultPreset(): WindowPreset {
        val presets = getPresets()
        val defaultId = getDefaultPresetId()
        return presets.firstOrNull { it.id == defaultId } ?: presets.firstOrNull() ?: WindowPreset.DEFAULT_PRESETS[0]
    }

    // App rules
    fun getAppRules(): List<AppWindowRule> {
        val rawJson = prefs.getString(KEY_APP_RULES, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(rawJson)
            val list = mutableListOf<AppWindowRule>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    AppWindowRule(
                        packageName = obj.getString("packageName"),
                        appName = obj.getString("appName"),
                        presetId = obj.getString("presetId"),
                        isEnabled = obj.optBoolean("isEnabled", true)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveAppRule(rule: AppWindowRule) {
        val current = getAppRules().toMutableList()
        val index = current.indexOfFirst { it.packageName == rule.packageName }
        if (index >= 0) {
            current[index] = rule
        } else {
            current.add(rule)
        }
        saveAppRules(current)
    }

    fun deleteAppRule(packageName: String) {
        val current = getAppRules().filterNot { it.packageName == packageName }
        saveAppRules(current)
    }

    private fun saveAppRules(rules: List<AppWindowRule>) {
        val jsonArray = JSONArray()
        for (r in rules) {
            val obj = JSONObject().apply {
                put("packageName", r.packageName)
                put("appName", r.appName)
                put("presetId", r.presetId)
                put("isEnabled", r.isEnabled)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_APP_RULES, jsonArray.toString()).apply()
    }

    fun getPresetForApp(packageName: String): WindowPreset {
        val rule = getAppRules().firstOrNull { it.packageName == packageName && it.isEnabled }
        if (rule != null) {
            val matchedPreset = getPresets().firstOrNull { it.id == rule.presetId }
            if (matchedPreset != null) return matchedPreset
        }
        return getDefaultPreset()
    }

    // Dock & Engine Settings
    var isDockEnabled: Boolean
        get() = prefs.getBoolean(KEY_DOCK_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_DOCK_ENABLED, value).apply()

    var isDockOnRightSide: Boolean
        get() = prefs.getBoolean(KEY_DOCK_SIDE_RIGHT, true)
        set(value) = prefs.edit().putBoolean(KEY_DOCK_SIDE_RIGHT, value).apply()

    var dockOpacity: Int
        get() = prefs.getInt(KEY_DOCK_OPACITY, 80)
        set(value) = prefs.edit().putInt(KEY_DOCK_OPACITY, value).apply()

    var isDockAutoHide: Boolean
        get() = prefs.getBoolean(KEY_DOCK_AUTO_HIDE, true)
        set(value) = prefs.edit().putBoolean(KEY_DOCK_AUTO_HIDE, value).apply()

    var isHapticFeedbackEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK, value).apply()

    var isMultipleTaskFlagsEnabled: Boolean
        get() = prefs.getBoolean(KEY_MULTIPLE_TASK_FLAGS, true)
        set(value) = prefs.edit().putBoolean(KEY_MULTIPLE_TASK_FLAGS, value).apply()

    var isShizukuModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_SHIZUKU_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_SHIZUKU_MODE, value).apply()
}
