package com.example.util

import com.example.domain.model.Channel
import java.text.Normalizer
import java.util.Locale

object SearchFilter {

    fun filter(
        channels: List<Channel>,
        query: String,
        selectedLanguage: String? = null,
        selectedCategory: String? = null
    ): List<Channel> {
        val normalizedQuery = normalize(query)

        return channels.filter { channel ->
            // 1. Language filter
            val matchesLanguage = selectedLanguage.isNullOrBlank() ||
                    selectedLanguage.equals("All", ignoreCase = true) ||
                    channel.effectiveLanguage.equals(selectedLanguage, ignoreCase = true)

            // 2. Content Category filter
            val matchesCategory = selectedCategory.isNullOrBlank() ||
                    selectedCategory.equals("All", ignoreCase = true) ||
                    channel.contentCategory.equals(selectedCategory, ignoreCase = true) ||
                    channel.groupTitle.equals(selectedCategory, ignoreCase = true)

            // 3. Search query filter
            val matchesQuery = if (normalizedQuery.isBlank()) {
                true
            } else {
                val normalizedName = normalize(channel.name)
                val normalizedGroup = normalize(channel.groupTitle)
                val normalizedTvgId = normalize(channel.tvgId.orEmpty())
                val normalizedLang = normalize(channel.effectiveLanguage)
                val normalizedCat = normalize(channel.contentCategory)

                normalizedName.contains(normalizedQuery) ||
                        normalizedGroup.contains(normalizedQuery) ||
                        normalizedTvgId.contains(normalizedQuery) ||
                        normalizedLang.contains(normalizedQuery) ||
                        normalizedCat.contains(normalizedQuery)
            }

            matchesLanguage && matchesCategory && matchesQuery
        }
    }

    /**
     * Normalizes text by decomposing/recomposing into standard NFC Unicode format,
     * stripping duplicate whitespaces, and converting to lower case.
     * Accurately handles Tamil glyphs, combining diacritics, and regional characters.
     */
    fun normalize(input: String): String {
        if (input.isBlank()) return ""
        val nfc = Normalizer.normalize(input, Normalizer.Form.NFC)
        return nfc.trim().replace(Regex("\\s+"), " ").lowercase(Locale.getDefault())
    }
}
