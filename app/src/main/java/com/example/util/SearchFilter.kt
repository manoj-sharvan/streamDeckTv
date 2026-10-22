package com.example.util

import com.example.domain.model.Channel

object SearchFilter {

    fun filter(
        channels: List<Channel>,
        query: String,
        selectedLanguage: String? = null,
        selectedCategory: String? = null
    ): List<Channel> {
        val trimmedQuery = query.trim().lowercase()
        val hasQuery = trimmedQuery.isNotEmpty()
        val hasLang = !selectedLanguage.isNullOrBlank() && !selectedLanguage.equals("All", ignoreCase = true)
        val hasCat = !selectedCategory.isNullOrBlank() && !selectedCategory.equals("All", ignoreCase = true)

        if (!hasQuery && !hasLang && !hasCat) {
            return channels
        }

        return channels.filter { channel ->
            // 1. Language filter
            if (hasLang && !channel.effectiveLanguage.equals(selectedLanguage, ignoreCase = true)) {
                return@filter false
            }

            // 2. Content Category filter
            if (hasCat &&
                !channel.contentCategory.equals(selectedCategory, ignoreCase = true) &&
                !channel.groupTitle.equals(selectedCategory, ignoreCase = true)
            ) {
                return@filter false
            }

            // 3. Fast search query filter
            if (hasQuery) {
                channel.name.contains(trimmedQuery, ignoreCase = true) ||
                    channel.groupTitle.contains(trimmedQuery, ignoreCase = true) ||
                    channel.effectiveLanguage.contains(trimmedQuery, ignoreCase = true) ||
                    channel.contentCategory.contains(trimmedQuery, ignoreCase = true) ||
                    (!channel.tvgId.isNullOrEmpty() && channel.tvgId.contains(trimmedQuery, ignoreCase = true))
            } else {
                true
            }
        }
    }

    fun normalize(text: String): String {
        return text.trim().lowercase().replace(Regex("\\s+"), " ")
    }
}
