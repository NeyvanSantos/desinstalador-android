package com.neyvan.desinstalador.accessibility

import android.content.Context

internal class AccessibilityConsentStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun hasConsent(): Boolean = preferences.getBoolean(KEY_CONSENT, false)

    fun accept() {
        preferences.edit().putBoolean(KEY_CONSENT, true).apply()
    }

    fun revoke() {
        preferences.edit().putBoolean(KEY_CONSENT, false).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "accessibility_consent"
        const val KEY_CONSENT = "automation_disclosure_accepted"
    }
}
