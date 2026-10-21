package com.example.data.parser

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.regex.Pattern

data class ParsedChannel(
    val name: String,
    val streamUrl: String,
    val groupTitle: String = "General",
    val tvgId: String? = null,
    val tvgLogo: String? = null,
    val tvgLanguage: String? = null,
    val tvgCountry: String? = null
)

class M3uParser {

    private val tvgIdPattern = Pattern.compile("tvg-id=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE)
    private val tvgLogoPattern = Pattern.compile("tvg-logo=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE)
    private val groupTitlePattern = Pattern.compile("group-title=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE)
    private val tvgLangPattern = Pattern.compile("(?:tvg-language|tvg-lang|language)=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE)
    private val tvgCountryPattern = Pattern.compile("(?:tvg-country|country)=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE)

    /**
     * Parse an M3U or M3U8 string content into a list of [ParsedChannel].
     */
    fun parse(content: String): List<ParsedChannel> {
        return parse(content.byteInputStream(Charsets.UTF_8))
    }

    /**
     * Parse an M3U or M3U8 InputStream into a list of [ParsedChannel].
     * Streaming line-by-line to prevent high memory usage on large playlists.
     */
    fun parse(inputStream: InputStream): List<ParsedChannel> {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val channels = mutableListOf<ParsedChannel>()

        var pendingName: String? = null
        var pendingGroup: String? = null
        var pendingTvgId: String? = null
        var pendingTvgLogo: String? = null
        var pendingTvgLang: String? = null
        var pendingTvgCountry: String? = null

        var line = reader.readLine()
        while (line != null) {
            val trimmed = line.trim()

            if (trimmed.isEmpty()) {
                line = reader.readLine()
                continue
            }

            if (trimmed.startsWith("#EXTINF:", ignoreCase = true)) {
                // Parse EXTINF line
                // Format: #EXTINF:-1 tvg-id="..." tvg-logo="..." group-title="..." tvg-language="...",Channel Name
                val commaIndex = trimmed.indexOf(',')
                val attributesPart = if (commaIndex != -1) trimmed.substring(0, commaIndex) else trimmed
                val channelNamePart = if (commaIndex != -1 && commaIndex + 1 < trimmed.length) {
                    trimmed.substring(commaIndex + 1).trim()
                } else {
                    "Untitled Channel"
                }

                pendingName = channelNamePart.ifEmpty { "Untitled Channel" }
                pendingGroup = extractAttribute(attributesPart, groupTitlePattern)?.ifEmpty { "General" } ?: "General"
                pendingTvgId = extractAttribute(attributesPart, tvgIdPattern)
                pendingTvgLogo = extractAttribute(attributesPart, tvgLogoPattern)
                pendingTvgLang = extractAttribute(attributesPart, tvgLangPattern)
                pendingTvgCountry = extractAttribute(attributesPart, tvgCountryPattern)

            } else if (trimmed.startsWith("#")) {
                // Comment, metadata or VLC directive (e.g. #EXTVLCOPT:, #EXTM3U) - ignore or skip
            } else if (trimmed.startsWith("http://", ignoreCase = true) ||
                       trimmed.startsWith("https://", ignoreCase = true) ||
                       trimmed.startsWith("rtmp://", ignoreCase = true) ||
                       trimmed.startsWith("content://", ignoreCase = true) ||
                       trimmed.startsWith("file://", ignoreCase = true)
            ) {
                // Stream URL detected
                val streamUrl = trimmed
                val channelName = pendingName ?: "Channel ${channels.size + 1}"
                val groupTitle = pendingGroup ?: "General"

                channels.add(
                    ParsedChannel(
                        name = channelName,
                        streamUrl = streamUrl,
                        groupTitle = groupTitle,
                        tvgId = pendingTvgId,
                        tvgLogo = pendingTvgLogo,
                        tvgLanguage = pendingTvgLang,
                        tvgCountry = pendingTvgCountry
                    )
                )

                // Reset pending metadata for next entry
                pendingName = null
                pendingGroup = null
                pendingTvgId = null
                pendingTvgLogo = null
                pendingTvgLang = null
                pendingTvgCountry = null
            }

            line = reader.readLine()
        }

        return channels
    }

    private fun extractAttribute(source: String, pattern: Pattern): String? {
        val matcher = pattern.matcher(source)
        return if (matcher.find()) {
            matcher.group(1)?.trim()
        } else {
            null
        }
    }
}
