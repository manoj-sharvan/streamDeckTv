package com.example.data.parser

import java.util.Locale

object ContentCategoryClassifier {

    const val CAT_NEWS = "News"
    const val CAT_SPORTS = "Sports"
    const val CAT_MOVIES = "Movies"
    const val CAT_ENTERTAINMENT = "Entertainment"
    const val CAT_MUSIC = "Music"
    const val CAT_KIDS = "Kids"
    const val CAT_EDUCATION = "Education"
    const val CAT_RELIGIOUS = "Religious"
    const val CAT_GENERAL = "General"
    const val CAT_OTHER = "Other"

    val STANDARD_CATEGORIES = listOf(
        CAT_NEWS,
        CAT_SPORTS,
        CAT_MOVIES,
        CAT_ENTERTAINMENT,
        CAT_MUSIC,
        CAT_KIDS,
        CAT_EDUCATION,
        CAT_RELIGIOUS,
        CAT_GENERAL,
        CAT_OTHER
    )

    /**
     * Determines content category using group-title first, followed by strict word-boundary
     * matching on channel name and tvg-id.
     */
    fun classify(
        groupTitle: String?,
        channelName: String,
        tvgId: String? = null
    ): String {
        // 1. Group-Title inspection first
        if (!groupTitle.isNullOrBlank() && !groupTitle.equals("General", ignoreCase = true) && !groupTitle.equals("Undefined", ignoreCase = true)) {
            val fromGroup = matchCategoryFromText(groupTitle)
            if (fromGroup != null) return fromGroup
        }

        // 2. Channel Name and TVG-ID token matching
        val combined = "$channelName ${tvgId.orEmpty()}"
        val fromName = matchCategoryFromText(combined)
        if (fromName != null) return fromName

        // 3. Fallback
        return if (!groupTitle.isNullOrBlank() && groupTitle.isNotBlank()) {
            normalizeGroupName(groupTitle)
        } else {
            CAT_GENERAL
        }
    }

    private fun matchCategoryFromText(text: String): String? {
        val lower = text.lowercase(Locale.ROOT)

        // Strict word boundary regex: matches whole tokens only, avoiding false substring collisions
        if (Regex("\\b(news|seithi|seithigal|samachar|khabar|bulletin|headline|breaking|today|24x7|live\\s*news|wion|cnn|bbc|reuters|ndtv|polimer\\s*news|thanthi\\s*tv|puthiya\\s*thalaimurai|news18|news7)\\b", RegexOption.IGNORE_CASE).containsMatchIn(lower)) {
            return CAT_NEWS
        }
        if (Regex("\\b(sports?|cricket|football|soccer|golf|tennis|arena|stadium|espn|star\\s*sports|sony\\s*ten|sony\\s*six|dd\\s*sports)\\b", RegexOption.IGNORE_CASE).containsMatchIn(lower)) {
            return CAT_SPORTS
        }
        if (Regex("\\b(movies?|cinema|talkies|film|action|thriller|classic|gold|box\\s*office|cine|ktv|j\\s*movies|raj\\s*digital|zee\\s*cinema|star\\s*gold)\\b", RegexOption.IGNORE_CASE).containsMatchIn(lower)) {
            return CAT_MOVIES
        }
        if (Regex("\\b(music|isai|gaana|songs?|melody|radio|fm|beat|tune|hits|isaiaruvi|sun\\s*music|raj\\s*musix|mtv|9xm|zoom)\\b", RegexOption.IGNORE_CASE).containsMatchIn(lower)) {
            return CAT_MUSIC
        }
        if (Regex("\\b(kids?|cartoon|chutti|toon|animation|disney|pogo|nick|junior|hungama|animax|baby|discovery\\s*kids)\\b", RegexOption.IGNORE_CASE).containsMatchIn(lower)) {
            return CAT_KIDS
        }
        if (Regex("\\b(education|kalvi|learning|study|knowledge|discovery|science|history|nature|docu|documentary|nat\\s*geo|national\\s*geographic|animal\\s*planet)\\b", RegexOption.IGNORE_CASE).containsMatchIn(lower)) {
            return CAT_EDUCATION
        }
        if (Regex("\\b(religious|spiritual|bhakti|bhakthi|bakthi|aanmeegam|aathmeega|devotion|devotional|pooja|prayer|darshan|god|shree|peace|dharma|quran|bible|church|hindu|islam|christian|temple|masjid|satsang|divya|madhav|sri\\s*sankara|vedic|subhavaartha|nambikkai)\\b", RegexOption.IGNORE_CASE).containsMatchIn(lower)) {
            return CAT_RELIGIOUS
        }
        if (Regex("\\b(entertainment|vijay|sun\\s*tv|zee|colors|sab\\s*tv|star\\s*plus|sony\\s*tv|comedy|siripoli|adithya|drama|serials?|kalaignar|jaya\\s*tv|raj\\s*tv|polimer\\s*tv)\\b", RegexOption.IGNORE_CASE).containsMatchIn(lower)) {
            return CAT_ENTERTAINMENT
        }

        return null
    }

    private fun normalizeGroupName(name: String): String {
        val clean = name.trim()
        return clean.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }
}
