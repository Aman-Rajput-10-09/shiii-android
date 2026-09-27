package com.aman.shiii_android.ui.character

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object ShiiiOverlaySettings {
    private const val PREFS_NAME = "shiii_character_prefs"
    private const val KEY_VISIBLE = "shiii_visible"

    private val _isShiiiVisible = MutableStateFlow(true)
    val isShiiiVisible = _isShiiiVisible.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isShiiiVisible.value = prefs.getBoolean(KEY_VISIBLE, true)
    }

    fun toggleVisibility(context: Context) {
        val newState = !_isShiiiVisible.value
        _isShiiiVisible.value = newState
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_VISIBLE, newState).apply()
    }

    fun setVisibility(context: Context, visible: Boolean) {
        _isShiiiVisible.value = visible
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_VISIBLE, visible).apply()
    }
}
