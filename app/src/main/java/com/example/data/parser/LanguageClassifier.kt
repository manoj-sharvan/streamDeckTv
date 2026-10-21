package com.example.data.parser

import java.util.Locale

object LanguageClassifier {

    const val LANG_TAMIL = "Tamil"
    const val LANG_ENGLISH = "English"
    const val LANG_HINDI = "Hindi"
    const val LANG_TELUGU = "Telugu"
    const val LANG_MALAYALAM = "Malayalam"
    const val LANG_KANNADA = "Kannada"
    const val LANG_BENGALI = "Bengali"
    const val LANG_MARATHI = "Marathi"
    const val LANG_PUNJABI = "Punjabi"
    const val LANG_OTHER = "Other"

    val KNOWN_LANGUAGES = listOf(
        LANG_TAMIL,
        LANG_ENGLISH,
        LANG_HINDI,
        LANG_TELUGU,
        LANG_MALAYALAM,
        LANG_KANNADA,
        LANG_BENGALI,
        LANG_MARATHI,
        LANG_PUNJABI,
        LANG_OTHER
    )

    data class ClassificationResult(
        val primaryLanguage: String,
        val allLanguages: List<String>,
        val isVerified: Boolean
    )

    /**
     * Classifies a channel into language category using M3U attributes, Unicode scripts,
     * channel name keywords, and playlist declared language context.
     */
    fun classify(
        channelName: String,
        tvgLanguage: String? = null,
        tvgId: String? = null,
        groupTitle: String? = null,
        playlistDeclaredLanguage: String? = null,
        userOverride: String? = null
    ): ClassificationResult {
        // 1. User manual override has highest priority
        if (!userOverride.isNullOrBlank()) {
            val normalized = normalizeLanguageName(userOverride.trim())
            return ClassificationResult(
                primaryLanguage = normalized,
                allLanguages = listOf(normalized),
                isVerified = true
            )
        }

        val detectedLanguages = mutableListOf<String>()

        // 2. Parse explicit M3U attributes (e.g. tvg-language="Tamil;English", tvg-lang="tam")
        if (!tvgLanguage.isNullOrBlank()) {
            val tokens = tvgLanguage.split(";", ",", "/", "|").map { it.trim().lowercase(Locale.ROOT) }
            for (token in tokens) {
                val mapped = mapLanguageCode(token)
                if (mapped != null && mapped !in detectedLanguages) {
                    detectedLanguages.add(mapped)
                }
            }
        }

        // 3. Detect Indic or other Unicode scripts directly from channel name
        val scriptLang = detectScriptLanguage(channelName)
        if (scriptLang != null && scriptLang !in detectedLanguages) {
            detectedLanguages.add(scriptLang)
        }

        // 4. Token & whole-word regex matching on channel name and metadata
        val combinedText = "${channelName.lowercase(Locale.ROOT)} ${tvgId.orEmpty().lowercase(Locale.ROOT)} ${groupTitle.orEmpty().lowercase(Locale.ROOT)}"
        val keywordLangs = detectKeywordLanguages(combinedText)
        for (lang in keywordLangs) {
            if (lang !in detectedLanguages) {
                detectedLanguages.add(lang)
            }
        }

        // If verified from channel's own metadata, name or script
        if (detectedLanguages.isNotEmpty()) {
            return ClassificationResult(
                primaryLanguage = detectedLanguages.first(),
                allLanguages = detectedLanguages,
                isVerified = true
            )
        }

        // 5. Context from playlist's declared language (e.g. Tamil TV default playlist)
        if (!playlistDeclaredLanguage.isNullOrBlank()) {
            val declared = normalizeLanguageName(playlistDeclaredLanguage)

            // Guard: Check if the channel is visibly English or International before blindly tagging it
            if (isObviousInternationalOrEnglish(combinedText)) {
                return ClassificationResult(
                    primaryLanguage = LANG_ENGLISH,
                    allLanguages = listOf(LANG_ENGLISH),
                    isVerified = false
                )
            }

            return ClassificationResult(
                primaryLanguage = declared,
                allLanguages = listOf(declared),
                isVerified = false
            )
        }

        // 6. Reliable fallback: classify as "Other"
        return ClassificationResult(
            primaryLanguage = LANG_OTHER,
            allLanguages = listOf(LANG_OTHER),
            isVerified = false
        )
    }

    private fun mapLanguageCode(token: String): String? {
        val clean = token.trim().lowercase(Locale.ROOT)
        return when (clean) {
            "tam", "ta", "tamil", "tamizh", "thamizh" -> LANG_TAMIL
            "eng", "en", "english" -> LANG_ENGLISH
            "hin", "hi", "hindi" -> LANG_HINDI
            "tel", "te", "telugu" -> LANG_TELUGU
            "mal", "ml", "malayalam" -> LANG_MALAYALAM
            "kan", "kn", "kannada" -> LANG_KANNADA
            "ben", "bn", "bengali", "bangla" -> LANG_BENGALI
            "mar", "mr", "marathi" -> LANG_MARATHI
            "pan", "pa", "punjabi" -> LANG_PUNJABI
            else -> null
        }
    }

    private fun detectScriptLanguage(text: String): String? {
        for (char in text) {
            when (char.code) {
                in 0x0B80..0x0BFF -> return LANG_TAMIL
                in 0x0C00..0x0C7F -> return LANG_TELUGU
                in 0x0D00..0x0D7F -> return LANG_MALAYALAM
                in 0x0C80..0x0CFF -> return LANG_KANNADA
                in 0x0980..0x09FF -> return LANG_BENGALI
                in 0x0A00..0x0A7F -> return LANG_PUNJABI
                in 0x0900..0x097F -> return LANG_HINDI // Devanagari (Hindi default)
            }
        }
        return null
    }

    private fun detectKeywordLanguages(text: String): List<String> {
        val results = mutableListOf<String>()

        // Word boundary regex ensures we do not match substring inside unrelated words
        if (Regex("\\b(tamil|tamizh|thamizh|ktv|suntv|sun\\s*music|sun\\s*news|chutti|adithya|kalaignar|jaya|polimer|thanthi|puthiya|vasanth|podhigai|makkal|vendhar|peppers|velicham|murasu|seithigal|raj\\s*tv|isaiaruvi|sirippoli)\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)) {
            results.add(LANG_TAMIL)
        }
        if (Regex("\\b(english|bbc|cnn|cnbc|aljazeera|bloomberg|discovery|natgeo|reuters|dw|france24|sky\\s*news|wion)\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)) {
            results.add(LANG_ENGLISH)
        }
        if (Regex("\\b(hindi|aajtak|abp\\s*news|ndtv|zeenews|colors|starplus|sab\\s*tv|sony\\s*tv|dangal)\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)) {
            results.add(LANG_HINDI)
        }
        if (Regex("\\b(telugu|etv|maa\\s*tv|gemini|sakshi|tv9\\s*telugu|v6\\s*news)\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)) {
            results.add(LANG_TELUGU)
        }
        if (Regex("\\b(malayalam|asianet|surya|manorama|mathrubhumi|kairali|mazhavil)\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)) {
            results.add(LANG_MALAYALAM)
        }
        if (Regex("\\b(kannada|udaya|suvarna|tv9\\s*kannada|dighvijay)\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)) {
            results.add(LANG_KANNADA)
        }
        if (Regex("\\b(bengali|bangla|abpananda|zeebangla|colors\\s*bangla)\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)) {
            results.add(LANG_BENGALI)
        }
        if (Regex("\\b(marathi|zeemarathi|abpmajha|colors\\s*marathi)\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)) {
            results.add(LANG_MARATHI)
        }
        if (Regex("\\b(punjabi|ptc|zeepunjabi|mh1)\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)) {
            results.add(LANG_PUNJABI)
        }

        return results
    }

    private fun isObviousInternationalOrEnglish(text: String): Boolean {
        return Regex("\\b(bbc|cnn|cnbc|aljazeera|bloomberg|dw|france24|reuters|sky\\s*news|euro\\s*news|nhk|cctv|cgtn|rt\\s*news)\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)
    }

    fun normalizeLanguageName(name: String): String {
        return when (name.trim().lowercase(Locale.ROOT)) {
            "tam", "ta", "tamil", "tamizh", "thamizh" -> LANG_TAMIL
            "eng", "en", "english" -> LANG_ENGLISH
            "hin", "hi", "hindi" -> LANG_HINDI
            "tel", "te", "telugu" -> LANG_TELUGU
            "mal", "ml", "malayalam" -> LANG_MALAYALAM
            "kan", "kn", "kannada" -> LANG_KANNADA
            "ben", "bn", "bengali", "bangla" -> LANG_BENGALI
            "mar", "mr", "marathi" -> LANG_MARATHI
            "pan", "pa", "punjabi" -> LANG_PUNJABI
            "other", "unknown" -> LANG_OTHER
            else -> name.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        }
    }
}
