package com.example.balaji

import android.content.Context
import android.content.SharedPreferences

object PrefUtils {

    private const val PREF_NAME = "AppPrefs"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val API_KEY = "ApiKey"
    fun getApiKey(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(API_KEY, "") ?: ""
    }

    fun isLoggedIn(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun setLoggedIn(context: Context, isLoggedIn: Boolean, apiKey: String) {
        val editor: SharedPreferences.Editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        editor.putString(API_KEY, apiKey)
        editor.apply()
    }
}
