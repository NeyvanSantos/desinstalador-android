package com.neyvan.desinstalador.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InstalledAppCategoryTest {

    @Test
    fun `category uses system flags and distinguishes updated system apps`() {
        assertEquals(
            InstalledAppCategory.USER_APP,
            classifyInstalledApp("com.example.notes", isSystemApp = false, isUpdatedSystemApp = false)
        )
        assertEquals(
            InstalledAppCategory.SYSTEM_APP,
            classifyInstalledApp("com.example.oemtool", isSystemApp = true, isUpdatedSystemApp = false)
        )
        assertEquals(
            InstalledAppCategory.UPDATED_SYSTEM_APP,
            classifyInstalledApp("com.example.oemtool", isSystemApp = false, isUpdatedSystemApp = true)
        )
    }

    @Test
    fun `critical packages stay protected regardless of package flags`() {
        assertEquals(
            InstalledAppCategory.CRITICAL_SYSTEM_APP,
            classifyInstalledApp("com.android.settings", isSystemApp = false, isUpdatedSystemApp = false)
        )
        assertTrue(CriticalSystemPackages.contains("com.google.android.packageinstaller"))
    }
}
