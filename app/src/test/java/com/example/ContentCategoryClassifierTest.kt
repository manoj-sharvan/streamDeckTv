package com.example

import com.example.data.parser.ContentCategoryClassifier
import org.junit.Assert.assertEquals
import org.junit.Test

class ContentCategoryClassifierTest {

    @Test
    fun testClassifiesByGroupTitle() {
        assertEquals(
            ContentCategoryClassifier.CAT_NEWS,
            ContentCategoryClassifier.classify(groupTitle = "News", channelName = "Channel 1")
        )

        assertEquals(
            ContentCategoryClassifier.CAT_SPORTS,
            ContentCategoryClassifier.classify(groupTitle = "Sports", channelName = "Channel 2")
        )

        assertEquals(
            ContentCategoryClassifier.CAT_MOVIES,
            ContentCategoryClassifier.classify(groupTitle = "Cinema", channelName = "Channel 3")
        )
    }

    @Test
    fun testClassifiesByChannelNameKeywords() {
        assertEquals(
            ContentCategoryClassifier.CAT_NEWS,
            ContentCategoryClassifier.classify(groupTitle = null, channelName = "Puthiya Thalaimurai News")
        )

        assertEquals(
            ContentCategoryClassifier.CAT_SPORTS,
            ContentCategoryClassifier.classify(groupTitle = null, channelName = "Star Sports Tamil HD")
        )

        assertEquals(
            ContentCategoryClassifier.CAT_MOVIES,
            ContentCategoryClassifier.classify(groupTitle = null, channelName = "KTV Movies HD")
        )

        assertEquals(
            ContentCategoryClassifier.CAT_MUSIC,
            ContentCategoryClassifier.classify(groupTitle = null, channelName = "Sun Music")
        )

        assertEquals(
            ContentCategoryClassifier.CAT_KIDS,
            ContentCategoryClassifier.classify(groupTitle = null, channelName = "Chithiram Kids TV")
        )

        assertEquals(
            ContentCategoryClassifier.CAT_RELIGIOUS,
            ContentCategoryClassifier.classify(groupTitle = null, channelName = "Bhakthi TV")
        )
    }

    @Test
    fun testWordBoundaryPreventsSpuriousSubstringMatching() {
        // "medicine" contains "cine" but should NOT match Movies
        val category = ContentCategoryClassifier.classify(
            groupTitle = null,
            channelName = "Health Medicine Channel"
        )
        // Should not be classified as Movies
        assertEquals(ContentCategoryClassifier.CAT_GENERAL, category)
    }

    @Test
    fun testFallbackToGeneralWhenNoCategoryMatches() {
        val category = ContentCategoryClassifier.classify(
            groupTitle = null,
            channelName = "Random Channel Stream"
        )
        assertEquals(ContentCategoryClassifier.CAT_GENERAL, category)
    }
}
