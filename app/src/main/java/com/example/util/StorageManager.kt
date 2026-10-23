package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * Storage and Cache Information data models.
 */
data class StorageInfo(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val usedPercentage: Int,
    val totalFormatted: String,
    val usedFormatted: String,
    val availableFormatted: String,
    val isLowStorage: Boolean,
    val isCriticallyLow: Boolean,
    val partitionName: String
)

data class AppCacheInfo(
    val cacheBytes: Long,
    val cacheFormatted: String,
    val fileCount: Int
)

data class CacheClearResult(
    val success: Boolean,
    val bytesDeleted: Long,
    val bytesDeletedFormatted: String,
    val filesDeletedCount: Int,
    val errorMessage: String? = null
)

/**
 * StorageManager:
 * Handles device filesystem storage measurement via StatFs, safe recursive app-specific
 * cache measurement & deletion, and Android TV system settings intent launching.
 */
object StorageManager {

    /**
     * Measure the actual filesystem storage of the internal data partition (/data).
     * Distinguishes the measured filesystem capacity from marketed device capacity.
     */
    fun getStorageInfo(context: Context): StorageInfo {
        return try {
            val dataPath = Environment.getDataDirectory()
            val stat = StatFs(dataPath.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = (totalBlocks * blockSize).coerceAtLeast(0L)
            val availableBytes = (availableBlocks * blockSize).coerceAtLeast(0L)
            val usedBytes = (totalBytes - availableBytes).coerceAtLeast(0L)

            val usedPercentage = if (totalBytes > 0L) {
                ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt().coerceIn(0, 100)
            } else 0

            val oneGb = 1024L * 1024L * 1024L
            val criticalThreshold = 400L * 1024L * 1024L

            StorageInfo(
                totalBytes = totalBytes,
                availableBytes = availableBytes,
                usedBytes = usedBytes,
                usedPercentage = usedPercentage,
                totalFormatted = formatBytes(totalBytes),
                usedFormatted = formatBytes(usedBytes),
                availableFormatted = formatBytes(availableBytes),
                isLowStorage = availableBytes < oneGb,
                isCriticallyLow = availableBytes < criticalThreshold,
                partitionName = "Internal Storage (/data)"
            )
        } catch (e: Exception) {
            StorageInfo(
                totalBytes = 0L,
                availableBytes = 0L,
                usedBytes = 0L,
                usedPercentage = 0,
                totalFormatted = "Unavailable",
                usedFormatted = "Unavailable",
                availableFormatted = "Unavailable",
                isLowStorage = false,
                isCriticallyLow = false,
                partitionName = "Internal Storage"
            )
        }
    }

    /**
     * Compute current temporary application cache size asynchronously.
     * Only calculates designated cache directories (cacheDir, codeCacheDir, externalCacheDir).
     * Excludes persistent databases, user playlists, and settings.
     */
    suspend fun getAppCacheInfo(context: Context): AppCacheInfo = withContext(Dispatchers.IO) {
        var totalBytes = 0L
        var totalCount = 0

        val dirs = listOfNotNull(
            context.cacheDir,
            context.codeCacheDir,
            try { context.externalCacheDir } catch (_: Exception) { null }
        )

        for (dir in dirs) {
            if (dir.exists() && dir.isDirectory) {
                val (dirBytes, dirCount) = calculateDirSize(dir)
                totalBytes += dirBytes
                totalCount += dirCount
            }
        }

        AppCacheInfo(
            cacheBytes = totalBytes,
            cacheFormatted = formatBytes(totalBytes),
            fileCount = totalCount
        )
    }

    private fun calculateDirSize(dir: File): Pair<Long, Int> {
        var size = 0L
        var count = 0
        val files = dir.listFiles() ?: return Pair(0L, 0)
        for (file in files) {
            try {
                if (file.isDirectory) {
                    val (subSize, subCount) = calculateDirSize(file)
                    size += subSize
                    count += subCount
                } else {
                    size += file.length()
                    count++
                }
            } catch (_: Exception) {}
        }
        return Pair(size, count)
    }

    /**
     * Safely delete files within this application's cache directories.
     * Preserves databases (Room), SharedPreferences, DataStore, and persistent user files.
     */
    suspend fun clearAppCache(context: Context): CacheClearResult = withContext(Dispatchers.IO) {
        var totalDeletedBytes = 0L
        var totalDeletedFiles = 0
        var hadErrors = false

        val dirs = listOfNotNull(
            context.cacheDir,
            context.codeCacheDir,
            try { context.externalCacheDir } catch (_: Exception) { null }
        )

        for (dir in dirs) {
            if (dir.exists() && dir.isDirectory) {
                val (deletedBytes, deletedFiles, errors) = deleteContentsOfDir(dir)
                totalDeletedBytes += deletedBytes
                totalDeletedFiles += deletedFiles
                if (errors) hadErrors = true
            }
        }

        CacheClearResult(
            success = !hadErrors,
            bytesDeleted = totalDeletedBytes,
            bytesDeletedFormatted = formatBytes(totalDeletedBytes),
            filesDeletedCount = totalDeletedFiles,
            errorMessage = if (hadErrors) "Some files could not be removed because they were locked by active media playback" else null
        )
    }

    private fun deleteContentsOfDir(dir: File): Triple<Long, Int, Boolean> {
        var deletedBytes = 0L
        var deletedCount = 0
        var error = false
        val files = dir.listFiles() ?: return Triple(0L, 0, false)
        for (file in files) {
            try {
                if (file.isDirectory) {
                    val (subBytes, subCount, subError) = deleteContentsOfDir(file)
                    deletedBytes += subBytes
                    deletedCount += subCount
                    if (subError) error = true
                    file.delete()
                } else {
                    val len = file.length()
                    if (file.delete()) {
                        deletedBytes += len
                        deletedCount++
                    } else {
                        error = true
                    }
                }
            } catch (e: Exception) {
                error = true
            }
        }
        return Triple(deletedBytes, deletedCount, error)
    }

    /**
     * Open Android TV's native system storage settings screen.
     * Attempts Settings.ACTION_INTERNAL_STORAGE_SETTINGS first, falling back to Settings.ACTION_SETTINGS.
     */
    fun openSystemStorageSettings(context: Context): Boolean {
        // 1. Try ACTION_INTERNAL_STORAGE_SETTINGS
        try {
            val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return true
        } catch (_: Exception) {}

        // 2. Try generic Settings.ACTION_SETTINGS
        try {
            val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallbackIntent)
            return true
        } catch (e: Exception) {
            try {
                Toast.makeText(
                    context,
                    "Unable to open Storage Settings on this device: ${e.localizedMessage ?: "Activity not found"}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (_: Exception) {}
            return false
        }
    }

    /**
     * Open Android TV's native App Info / Application details screen for StreamDeck TV.
     */
    fun openAppDetailsSettings(context: Context): Boolean {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            return openSystemStorageSettings(context)
        }
    }

    /**
     * Format byte values into readable units with consistent decimal precision.
     */
    fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
        return when {
            digitGroups >= 3 -> String.format(Locale.US, "%.2f %s", value, units[digitGroups])
            digitGroups == 2 -> String.format(Locale.US, "%.1f %s", value, units[digitGroups])
            else -> String.format(Locale.US, "%.0f %s", value, units[digitGroups])
        }
    }
}
