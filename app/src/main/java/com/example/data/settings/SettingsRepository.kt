package com.example.data.settings

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    companion object {
        val INCOGNITO_MODE = booleanPreferencesKey("incognito_mode")
        val THEME_MODE = intPreferencesKey("theme_mode") // 0: Auto, 1: Light, 2: Dark
        val NUMBER_FORMAT = intPreferencesKey("number_format") // 0: Decimal, 1: Integer, 2: Incognito
        val LANGUAGE = stringPreferencesKey("language") // "uk", "en"
        val USD_RATE = floatPreferencesKey("usd_rate")
        val EUR_RATE = floatPreferencesKey("eur_rate")
        val PLN_RATE = floatPreferencesKey("pln_rate")
        val GBP_RATE = floatPreferencesKey("gbp_rate")
        val NOTIFICATION_PARSER_ENABLED = booleanPreferencesKey("notification_parser_enabled")
    }

    val incognitoModeFlow: Flow<Boolean> = context.dataStore.data.map { 
        val format = it[NUMBER_FORMAT] ?: (if (it[INCOGNITO_MODE] == true) 2 else 0)
        format == 2
    }
    val numberFormatFlow: Flow<Int> = context.dataStore.data.map { 
        it[NUMBER_FORMAT] ?: (if (it[INCOGNITO_MODE] == true) 2 else 0)
    }
    val languageFlow: Flow<String> = context.dataStore.data.map { it[LANGUAGE] ?: "uk" }
    val themeModeFlow: Flow<Int> = context.dataStore.data.map { it[THEME_MODE] ?: 0 }
    val notificationParserEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[NOTIFICATION_PARSER_ENABLED] ?: false }
    
    val notificationParserAppsFlow: Flow<String> = context.dataStore.data.map { 
        it[stringPreferencesKey("notification_parser_apps")] ?: "com.ftband.mono,ua.privatbank.ap24"
    }
    
    val usdRateFlow: Flow<Float> = context.dataStore.data.map { it[USD_RATE] ?: 40.0f }
    val eurRateFlow: Flow<Float> = context.dataStore.data.map { it[EUR_RATE] ?: 42.5f }
    val plnRateFlow: Flow<Float> = context.dataStore.data.map { it[PLN_RATE] ?: 9.5f }
    val gbpRateFlow: Flow<Float> = context.dataStore.data.map { it[GBP_RATE] ?: 50.0f }

    suspend fun setIncognitoMode(enabled: Boolean) {
        val format = if (enabled) 2 else 0
        context.dataStore.edit { 
            it[INCOGNITO_MODE] = enabled 
            it[NUMBER_FORMAT] = format
        }
    }

    suspend fun setNumberFormat(format: Int) {
        context.dataStore.edit { 
            it[NUMBER_FORMAT] = format 
            it[INCOGNITO_MODE] = (format == 2)
        }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[LANGUAGE] = lang }
    }

    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setNotificationParserEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATION_PARSER_ENABLED] = enabled }
    }

    suspend fun setNotificationParserApps(apps: String) {
        context.dataStore.edit { it[stringPreferencesKey("notification_parser_apps")] = apps }
    }

    fun getAccountKeywordsFlow(accountId: Int): Flow<String> = context.dataStore.data.map {
        it[stringPreferencesKey("account_keywords_$accountId")] ?: ""
    }

    suspend fun setAccountKeywords(accountId: Int, keywords: String) {
        context.dataStore.edit { it[stringPreferencesKey("account_keywords_$accountId")] = keywords }
    }

    fun getCategoryKeywordsFlow(categoryId: Int): Flow<String> = context.dataStore.data.map {
        it[stringPreferencesKey("category_keywords_$categoryId")] ?: ""
    }

    suspend fun setCategoryKeywords(categoryId: Int, keywords: String) {
        context.dataStore.edit { it[stringPreferencesKey("category_keywords_$categoryId")] = keywords }
    }
    
    suspend fun setRate(currency: String, rate: Float) {
        context.dataStore.edit { prefs ->
            when (currency) {
                "USD" -> prefs[USD_RATE] = rate
                "EUR" -> prefs[EUR_RATE] = rate
                "PLN" -> prefs[PLN_RATE] = rate
                "GBP" -> prefs[GBP_RATE] = rate
            }
        }
    }
}
