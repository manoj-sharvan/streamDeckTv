package com.example.data.parser

import java.net.URI

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}

object PlaylistValidator {

    fun validateUrl(rawUrl: String): ValidationResult {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Playlist URL cannot be empty.")
        }
        if (!trimmed.startsWith("http://", ignoreCase = true) && !trimmed.startsWith("https://", ignoreCase = true)) {
            return ValidationResult.Invalid("URL must start with http:// or https://")
        }
        return try {
            val uri = URI(trimmed)
            if (uri.host.isNullOrBlank()) {
                ValidationResult.Invalid("Invalid URL: missing host address.")
            } else {
                ValidationResult.Valid
            }
        } catch (e: Exception) {
            ValidationResult.Invalid("Malformed URL: ${e.localizedMessage ?: "Invalid URL format"}")
        }
    }

    fun normalizeUrl(rawUrl: String): String {
        return rawUrl.trim()
    }

    fun derivePlaylistNameFromUrl(url: String): String {
        return try {
            val uri = URI(url.trim())
            val path = uri.path ?: ""
            val lastSegment = path.substringAfterLast('/').substringBeforeLast('.')
            if (lastSegment.isNotBlank()) {
                lastSegment.replace('_', ' ').replace('-', ' ')
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            } else {
                uri.host ?: "Custom Playlist"
            }
        } catch (e: Exception) {
            "Custom Playlist"
        }
    }
}
