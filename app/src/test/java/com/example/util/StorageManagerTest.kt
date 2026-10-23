package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class StorageManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testFormatBytes_zeroAndNegative() {
        assertEquals("0 B", StorageManager.formatBytes(0L))
        assertEquals("0 B", StorageManager.formatBytes(-500L))
    }

    @Test
    fun testFormatBytes_bytesAndKilobytes() {
        assertEquals("512 B", StorageManager.formatBytes(512L))
        assertEquals("1 KB", StorageManager.formatBytes(1024L))
        assertEquals("15 KB", StorageManager.formatBytes(15 * 1024L))
    }

    @Test
    fun testFormatBytes_megabytes() {
        val mb = 1024L * 1024L
        assertEquals("1.0 MB", StorageManager.formatBytes(mb))
        assertEquals("45.5 MB", StorageManager.formatBytes((45.5 * mb).toLong()))
    }

    @Test
    fun testFormatBytes_gigabytes() {
        val gb = 1024L * 1024L * 1024L
        assertEquals("1.00 GB", StorageManager.formatBytes(gb))
        assertEquals("2.14 GB", StorageManager.formatBytes((2.14 * gb).toLong()))
        assertEquals("8.00 GB", StorageManager.formatBytes(8L * gb))
    }

    @Test
    fun testStorageInfoDataModelCalculations() {
        val total = 8L * 1024L * 1024L * 1024L
        val available = 2L * 1024L * 1024L * 1024L
        val used = total - available
        val percentage = ((used.toDouble() / total.toDouble()) * 100).toInt()

        val info = StorageInfo(
            totalBytes = total,
            availableBytes = available,
            usedBytes = used,
            usedPercentage = percentage,
            totalFormatted = StorageManager.formatBytes(total),
            usedFormatted = StorageManager.formatBytes(used),
            availableFormatted = StorageManager.formatBytes(available),
            isLowStorage = false,
            isCriticallyLow = false,
            partitionName = "Internal Storage (/data)"
        )

        assertEquals(8589934592L, info.totalBytes)
        assertEquals(2147483648L, info.availableBytes)
        assertEquals(6442450944L, info.usedBytes)
        assertEquals(75, info.usedPercentage)
        assertEquals("8.00 GB", info.totalFormatted)
        assertEquals("6.00 GB", info.usedFormatted)
        assertEquals("2.00 GB", info.availableFormatted)
        assertFalse(info.isLowStorage)
        assertFalse(info.isCriticallyLow)
    }

    @Test
    fun testLowStorageThresholds() {
        val criticalBytes = 300L * 1024L * 1024L // 300 MB
        val lowBytes = 800L * 1024L * 1024L // 800 MB
        val oneGb = 1024L * 1024L * 1024L

        assertTrue(criticalBytes < 400L * 1024L * 1024L)
        assertTrue(lowBytes < oneGb)
    }
}
