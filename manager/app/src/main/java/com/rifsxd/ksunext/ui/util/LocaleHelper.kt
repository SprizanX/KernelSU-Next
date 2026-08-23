package com.rifsxd.ksunext.ui.util

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LocaleHelper {
    
    /**
     * Check if should use system language settings (Android 13+)
     */
    val useSystemLanguageSettings: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    
    /**
     * Map legacy ISO language codes that are ill-formed for Locale.Builder
     * to their modern BCP 47 equivalents.
     */
    fun normalizeLegacyLanguageCode(code: String): String {
        return when (code) {
            "in" -> "id" // Indonesian
            "iw" -> "he" // Hebrew
            "ji" -> "yi" // Yiddish
            "jw" -> "jv" // Javanese
            else -> code
        }
    }

    /**
     * Persist the chosen locale.
     *
     * Android 13+: applied directly through LocaleManager so the app does not
     * depend on the OEM's (often broken) system locale picker screen.
     * Android < 13: stored in prefs and applied via [applyLanguage] on next
     * activity creation; caller should call refreshActivity() afterwards.
     */
    fun setPreferredLocale(context: Context, tag: String) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("app_locale", tag).apply()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val localeManager = context.getSystemService(Context.LOCALE_SERVICE) as? android.app.LocaleManager
                    ?: return
                if (tag == "system") {
                    localeManager.applicationLocales = android.os.LocaleList.getEmptyLocaleList()
                } else {
                    localeManager.applicationLocales = android.os.LocaleList(parseLocaleTag(tag))
                }
            } catch (_: Exception) {
                // Some ROMs may lack a working LocaleManager; prefs fallback remains
            }
        }
    }
    
    /**
     * Apply saved language setting to context (for Android < 13)
     */
    fun applyLanguage(context: Context): Context {
        // On Android 13+, language is handled by system
        if (useSystemLanguageSettings) {
            return context
        }
        
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val localeTag = prefs.getString("app_locale", "system") ?: "system"
        
        return if (localeTag == "system") {
            context
        } else {
            val locale = parseLocaleTag(localeTag)
            setLocale(context, locale)
        }
    }
    
    /**
     * Set locale for context (Android < 13)
     */
    private fun setLocale(context: Context, locale: Locale): Context {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, locale)
        } else {
            updateResourcesLegacy(context, locale)
        }
    }
    
    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context, locale: Locale): Context {
        val configuration = Configuration()
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }
    
    @Suppress("DEPRECATION")
    @SuppressWarnings("deprecation")
    private fun updateResourcesLegacy(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        val resources = context.resources
        val configuration = resources.configuration
        configuration.locale = locale
        configuration.setLayoutDirection(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }
    
    /**
     * Parse locale tag to Locale object
     */
    private fun parseLocaleTag(tag: String): Locale {
        return try {
            if (tag.contains("_")) {
                val parts = tag.split("_")
                Locale.Builder()
                    .setLanguage(normalizeLegacyLanguageCode(parts[0]))
                    .setRegion(parts.getOrNull(1) ?: "")
                    .build()
            } else {
                Locale.Builder()
                    .setLanguage(normalizeLegacyLanguageCode(tag))
                    .build()
            }
        } catch (_: Exception) {
            Locale.getDefault()
        }
    }
    
    /**
     * Get current app locale
     */
    fun getCurrentAppLocale(context: Context): Locale? {
        return if (useSystemLanguageSettings) {
            // Android 13+ - get from system app locale settings
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                try {
                    val localeManager = context.getSystemService(Context.LOCALE_SERVICE) as? android.app.LocaleManager
                    val locales = localeManager?.applicationLocales
                    if (locales != null && !locales.isEmpty) {
                        locales.get(0)
                    } else {
                        null // System default
                    }
                } catch (_: Exception) {
                    null // System default
                }
            } else {
                null // System default
            }
        } else {
            // Android < 13 - get from SharedPreferences
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val localeTag = prefs.getString("app_locale", "system") ?: "system"
            if (localeTag == "system") {
                null // System default
            } else {
                parseLocaleTag(localeTag)
            }
        }
    }
}