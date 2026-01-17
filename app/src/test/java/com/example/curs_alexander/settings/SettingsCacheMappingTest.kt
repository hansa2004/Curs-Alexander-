package com.example.curs_alexander.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SettingsCacheMappingTest {

    @Test
    fun `themeMode mapping matches enum names`() {
        fun map(v: String?): ThemeMode? = when (v) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            "SYSTEM" -> ThemeMode.SYSTEM
            else -> null
        }

        assertEquals(ThemeMode.LIGHT, map("LIGHT"))
        assertEquals(ThemeMode.DARK, map("DARK"))
        assertEquals(ThemeMode.SYSTEM, map("SYSTEM"))
        assertNull(map("UNKNOWN"))
        assertNull(map(null))
    }

    @Test
    fun `fontScale mapping matches enum names`() {
        fun map(v: String?): FontScale? = when (v) {
            "SMALL" -> FontScale.SMALL
            "MEDIUM" -> FontScale.MEDIUM
            "LARGE" -> FontScale.LARGE
            else -> null
        }

        assertEquals(FontScale.SMALL, map("SMALL"))
        assertEquals(FontScale.MEDIUM, map("MEDIUM"))
        assertEquals(FontScale.LARGE, map("LARGE"))
        assertNull(map("UNKNOWN"))
        assertNull(map(null))
    }
}

