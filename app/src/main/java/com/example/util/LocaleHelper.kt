package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {
    private const val PREF_NAME = "mi_freeform_prefs"
    private const val KEY_LANGUAGE = "app_language"

    const val LANG_SYSTEM = "system"
    const val LANG_EN = "en"
    const val LANG_ID = "in" // Android ISO code for Indonesian

    fun getSavedLanguage(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, LANG_SYSTEM) ?: LANG_SYSTEM
    }

    fun setLanguage(context: Context, languageCode: String): Context {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
        return updateResources(context, languageCode)
    }

    fun applySavedLocale(context: Context): Context {
        val lang = getSavedLanguage(context)
        return if (lang == LANG_SYSTEM) context else updateResources(context, lang)
    }

    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = when (languageCode) {
            LANG_ID, "id" -> Locale("in")
            LANG_EN -> Locale.ENGLISH
            else -> Locale.getDefault()
        }
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
            return context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            return context
        }
    }
}
