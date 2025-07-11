package com.kapilagro.sasyak.utils

import android.content.Context
import androidx.core.content.edit

object PreferencesHelper {
    private const val PREF_NAME = "SasyakPrefs"
    private const val KEY_PROFILE_POPUP_SHOWN = "profile_popup_shown"

    fun hasShownProfilePopup(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PROFILE_POPUP_SHOWN, false)
    }

    fun setProfilePopupShown(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_PROFILE_POPUP_SHOWN, true) }
    }

    fun clearProfilePopupShown(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_PROFILE_POPUP_SHOWN, false) }
    }
}