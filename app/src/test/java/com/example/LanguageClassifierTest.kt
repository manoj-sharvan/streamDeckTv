package com.example

import com.example.data.parser.LanguageClassifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageClassifierTest {

    @Test
    fun testDetectsTamilFromUnicodeScript() {
        val result = LanguageClassifier.classify(
            channelName = "சன் டிவி" // Tamil script
        )
        assertEquals(LanguageClassifier.LANG_TAMIL, result.primaryLanguage)
        assertTrue(result.isVerified)
    }

    @Test
    fun testDetectsTamilFromM3uLanguageAttribute() {
        val result = LanguageClassifier.classify(
            channelName = "Kalaignar TV",
            tvgLanguage = "tam"
        )
        assertEquals(LanguageClassifier.LANG_TAMIL, result.primaryLanguage)
        assertTrue(result.isVerified)
    }

    @Test
    fun testDetectsTamilFromChannelNameKeywords() {
        val result = LanguageClassifier.classify(
            channelName = "Polimer News Tamil"
        )
        assertEquals(LanguageClassifier.LANG_TAMIL, result.primaryLanguage)
        assertTrue(result.isVerified)
    }

    @Test
    fun testDetectsOtherIndianLanguages() {
        // Hindi Devanagari script
        assertEquals(
            LanguageClassifier.LANG_HINDI,
            LanguageClassifier.classify(channelName = "आज तक").primaryLanguage
        )

        // Telugu script
        assertEquals(
            LanguageClassifier.LANG_TELUGU,
            LanguageClassifier.classify(channelName = "ఈటీవీ ఆంధ్రప్రదేశ్").primaryLanguage
        )

        // Malayalam script
        assertEquals(
            LanguageClassifier.LANG_MALAYALAM,
            LanguageClassifier.classify(channelName = "ഏഷ്യാനെറ്റ് ന്യൂസ്").primaryLanguage
        )

        // Kannada script
        assertEquals(
            LanguageClassifier.LANG_KANNADA,
            LanguageClassifier.classify(channelName = "ಉದಯ ಟಿವಿ").primaryLanguage
        )
    }

    @Test
    fun testPlaylistDefaultLanguageWithoutChannelEvidenceIsNotVerified() {
        val result = LanguageClassifier.classify(
            channelName = "Random Channel Without Keywords",
            playlistDeclaredLanguage = LanguageClassifier.LANG_TAMIL
        )
        // Defaults to playlist language, but verified should be false
        assertEquals(LanguageClassifier.LANG_TAMIL, result.primaryLanguage)
        assertFalse(result.isVerified)
    }

    @Test
    fun testUnknownChannelsFallbackToOtherRatherThanGuessing() {
        val result = LanguageClassifier.classify(
            channelName = "XYZ Stream 101"
        )
        assertEquals(LanguageClassifier.LANG_OTHER, result.primaryLanguage)
        assertFalse(result.isVerified)
    }

    @Test
    fun testUserLanguageOverrideTakesPrecedence() {
        val result = LanguageClassifier.classify(
            channelName = "BBC World News",
            tvgLanguage = "eng",
            userOverride = "Tamil"
        )
        assertEquals("Tamil", result.primaryLanguage)
        assertTrue(result.isVerified)
    }
}
