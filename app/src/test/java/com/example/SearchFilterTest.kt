package com.example

import com.example.domain.model.Channel
import com.example.util.SearchFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchFilterTest {

    private val sampleChannels = listOf(
        Channel(
            id = 1,
            playlistId = 1,
            name = "Sun TV",
            streamUrl = "http://example.com/suntv",
            groupTitle = "Entertainment",
            language = "Tamil",
            contentCategory = "Entertainment",
            tvgId = "SunTV.in"
        ),
        Channel(
            id = 2,
            playlistId = 1,
            name = "சன் செய்திகள்", // Sun News in Tamil
            streamUrl = "http://example.com/sunnews",
            groupTitle = "News",
            language = "Tamil",
            contentCategory = "News",
            tvgId = "SunNews.in"
        ),
        Channel(
            id = 3,
            playlistId = 1,
            name = "Star Sports 1",
            streamUrl = "http://example.com/starsports",
            groupTitle = "Sports",
            language = "English",
            contentCategory = "Sports",
            tvgId = "StarSports.in"
        ),
        Channel(
            id = 4,
            playlistId = 1,
            name = "NDTV India",
            streamUrl = "http://example.com/ndtv",
            groupTitle = "News",
            language = "Hindi",
            contentCategory = "News",
            tvgId = "NDTVIndia.in"
        )
    )

    @Test
    fun testTamilUnicodeSearch() {
        val results = SearchFilter.filter(
            channels = sampleChannels,
            query = "செய்திகள்"
        )
        assertEquals(1, results.size)
        assertEquals("சன் செய்திகள்", results[0].name)
    }

    @Test
    fun testCaseInsensitiveAndWhitespaceNormalizedSearch() {
        val results = SearchFilter.filter(
            channels = sampleChannels,
            query = "   sun   tv   "
        )
        assertEquals(1, results.size)
        assertEquals("Sun TV", results[0].name)
    }

    @Test
    fun testLanguageFilter() {
        val tamilOnly = SearchFilter.filter(
            channels = sampleChannels,
            query = "",
            selectedLanguage = "Tamil"
        )
        assertEquals(2, tamilOnly.size)

        val englishOnly = SearchFilter.filter(
            channels = sampleChannels,
            query = "",
            selectedLanguage = "English"
        )
        assertEquals(1, englishOnly.size)
        assertEquals("Star Sports 1", englishOnly[0].name)
    }

    @Test
    fun testContentCategoryFilter() {
        val newsOnly = SearchFilter.filter(
            channels = sampleChannels,
            query = "",
            selectedCategory = "News"
        )
        assertEquals(2, newsOnly.size)
    }

    @Test
    fun testCombinedSearchAndFilter() {
        // News channels that are Tamil
        val result = SearchFilter.filter(
            channels = sampleChannels,
            query = "Sun",
            selectedLanguage = "Tamil",
            selectedCategory = "News"
        )
        // Only "Sun News" (tvgId SunNews.in) matches both query "Sun", language "Tamil", and category "News"
        assertEquals(1, result.size)
        assertEquals(2L, result[0].id)
    }

    @Test
    fun testNormalizeHandlesEmptyAndDiacritics() {
        assertEquals("", SearchFilter.normalize(""))
        assertEquals("", SearchFilter.normalize("   "))
        assertEquals("test channel", SearchFilter.normalize("  Test   Channel  "))
    }
}
