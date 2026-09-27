package com.aman.shiii_android.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

data class AppThemeColors(
    val isDark: Boolean,
    val backgroundGradient: List<Color>,
    val topBarBg: Color,
    val topBarTitle: Color,
    val topBarSubtitle: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val receivedBubbleBg: Color,
    val receivedBubbleText: Color,
    val receivedBubbleBorder: Color,
    val sentBubbleBg: Color,
    val sentBubbleText: Color,
    val inputBarBg: Color,
    val inputFieldBg: Color,
    val inputFieldText: Color,
    val inputFieldHint: Color,
    val bottomNavBg: Color,
    val bottomNavIndicator: Color,
    val bottomNavSelected: Color,
    val bottomNavUnselected: Color,
    val accentPink: Color,
    val chipBg: Color,
    val chipText: Color,
    val chipBorder: Color
)

object AppThemeManager {
    private const val PREFS_NAME = "shiii_theme_prefs"
    private const val KEY_MODE = "theme_mode"

    // Default to LIGHT so that every screen starts in the light mood requested by user
    private val _themeMode = MutableStateFlow(ThemeMode.LIGHT)
    val themeMode = _themeMode.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedMode = prefs.getString(KEY_MODE, ThemeMode.LIGHT.name) ?: ThemeMode.LIGHT.name
        _themeMode.value = runCatching { ThemeMode.valueOf(savedMode) }.getOrDefault(ThemeMode.LIGHT)
    }

    fun setThemeMode(context: Context, mode: ThemeMode) {
        _themeMode.value = mode
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_MODE, mode.name).apply()
    }

    fun toggleTheme(context: Context) {
        val current = _themeMode.value
        val next = if (current == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
        setThemeMode(context, next)
    }

    @Composable
    fun isDark(): Boolean {
        val mode by _themeMode.collectAsState()
        return when (mode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }
    }

    @Composable
    fun currentColors(): AppThemeColors {
        val dark = isDark()
        return if (dark) {
            // Premium Midnight Slate / Obsidian Theme
            AppThemeColors(
                isDark = true,
                backgroundGradient = listOf(
                    Color(0xFF0F1117),
                    Color(0xFF161922),
                    Color(0xFF11131A)
                ),
                topBarBg = Color(0xFF141722),
                topBarTitle = Color(0xFFF1F3F9),
                topBarSubtitle = Color(0xFFA2A9BE),
                cardBg = Color(0xFF1C202C),
                cardBorder = Color(0xFF2C3246),
                textPrimary = Color(0xFFF1F3F9),
                textSecondary = Color(0xFFA2A9BE),
                receivedBubbleBg = Color(0xFF212534),
                receivedBubbleText = Color(0xFFF1F3F9),
                receivedBubbleBorder = Color(0xFF32384E),
                sentBubbleBg = Color(0xFFE84E77),
                sentBubbleText = Color.White,
                inputBarBg = Color(0xFF13151E),
                inputFieldBg = Color(0xFF1C202C),
                inputFieldText = Color.White,
                inputFieldHint = Color(0xFF868FA6),
                bottomNavBg = Color(0xFF12141D),
                bottomNavIndicator = CherryBlossomPink.copy(alpha = 0.35f),
                bottomNavSelected = CherryBlossomPink,
                bottomNavUnselected = Color(0xFF7A839E),
                accentPink = CherryBlossomPink,
                chipBg = Color(0xFF1E2332),
                chipText = SoftRose,
                chipBorder = Color(0xFF333B52)
            )
        } else {
            // Elegant Light Sakura Theme (as in the screenshot for both Master and Mistress)
            AppThemeColors(
                isDark = false,
                backgroundGradient = listOf(
                    SakuraBlush,
                    Color(0xFFFFFFFF),
                    Color(0xFFFFF2F6)
                ),
                topBarBg = SakuraBlush,
                topBarTitle = DeepViolet,
                topBarSubtitle = TextDark.copy(alpha = 0.65f),
                cardBg = Color(0xFFFFFFFF),
                cardBorder = Color(0xFFFFD6E2),
                textPrimary = DeepViolet,
                textSecondary = TextDark.copy(alpha = 0.7f),
                receivedBubbleBg = Color(0xFFFFF0F5),
                receivedBubbleText = DeepViolet,
                receivedBubbleBorder = Color(0xFFFFDDE6),
                sentBubbleBg = CherryBlossomPink,
                sentBubbleText = Color.White,
                inputBarBg = Color.White,
                inputFieldBg = Color(0xFFFFF9FA),
                inputFieldText = DeepViolet,
                inputFieldHint = TextMuted,
                bottomNavBg = Color.White,
                bottomNavIndicator = CherryBlossomPink.copy(alpha = 0.22f),
                bottomNavSelected = CoralTender,
                bottomNavUnselected = Color(0xFF8C7B8A),
                accentPink = CherryBlossomPink,
                chipBg = Color(0xFFFFF0F5),
                chipText = DeepViolet,
                chipBorder = Color(0xFFFFD6E2)
            )
        }
    }
}
