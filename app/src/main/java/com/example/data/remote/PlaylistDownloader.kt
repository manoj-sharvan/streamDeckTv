package com.example.data.remote

import com.example.data.parser.PlaylistValidator
import com.example.data.parser.ValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.io.InputStream
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

sealed class DownloadResult {
    data class Success(val inputStream: InputStream, val playlistName: String) : DownloadResult()
    data class Error(val message: String, val throwable: Throwable? = null) : DownloadResult()
}

class PlaylistDownloader(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) {

    suspend fun download(url: String): DownloadResult = withContext(Dispatchers.IO) {
        val validation = PlaylistValidator.validateUrl(url)
        if (validation is ValidationResult.Invalid) {
            return@withContext DownloadResult.Error(validation.reason)
        }

        val sanitizedUrl = PlaylistValidator.normalizeUrl(url)

        try {
            val request = Request.Builder()
                .url(sanitizedUrl)
                .header("User-Agent", "StreamDeck-TV/1.0 (Android TV ExoPlayer)")
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                val detail = when (response.code) {
                    404 -> "Playlist not found (HTTP 404). Verify the URL link."
                    403 -> "Access denied (HTTP 403). The server restricts access."
                    500, 502, 503 -> "Server error (${response.code}). The playlist provider is currently unavailable."
                    else -> "Server returned HTTP ${response.code}: ${response.message}"
                }
                return@withContext DownloadResult.Error(detail)
            }

            val body = response.body
                ?: return@withContext DownloadResult.Error("Server returned empty playlist content.")

            // Derive a friendly playlist name from the URL path
            val pathSegments = request.url.pathSegments
            val lastSegment = pathSegments.lastOrNull()?.substringBefore(".m3u")?.substringBefore(".m3u8")
            val playlistName = if (!lastSegment.isNullOrBlank()) {
                lastSegment.replace('_', ' ').replace('-', ' ').capitalizeWords()
            } else {
                request.url.host
            }

            DownloadResult.Success(body.byteStream(), playlistName)
        } catch (e: UnknownHostException) {
            DownloadResult.Error("Could not resolve host. Check your TV internet connection or URL spelling.", e)
        } catch (e: SocketTimeoutException) {
            DownloadResult.Error("Connection timed out while downloading the playlist.", e)
        } catch (e: IOException) {
            DownloadResult.Error("Network error: ${e.localizedMessage ?: "Failed to connect"}", e)
        } catch (e: Exception) {
            DownloadResult.Error("Failed to fetch playlist: ${e.localizedMessage ?: "Unknown error"}", e)
        }
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
