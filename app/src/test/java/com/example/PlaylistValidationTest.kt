package com.example

import com.example.data.parser.PlaylistValidator
import com.example.data.parser.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistValidationTest {

    @Test
    fun testValidTamilTvHttpsUrl() {
        val url = "https://iptv-org.github.io/iptv/languages/tam.m3u"
        val result = PlaylistValidator.validateUrl(url)
        assertTrue("Valid HTTPS URL should pass", result is ValidationResult.Valid)
    }

    @Test
    fun testValidHttpUrl() {
        val url = "http://iptv.example.com/playlist.m3u8"
        val result = PlaylistValidator.validateUrl(url)
        assertTrue("Valid HTTP URL should pass", result is ValidationResult.Valid)
    }

    @Test
    fun testEmptyUrlRejected() {
        val result = PlaylistValidator.validateUrl("   ")
        assertTrue("Empty URL should be rejected", result is ValidationResult.Invalid)
        assertEquals("Playlist URL cannot be empty.", (result as ValidationResult.Invalid).reason)
    }

    @Test
    fun testMissingSchemeRejected() {
        val result = PlaylistValidator.validateUrl("iptv-org.github.io/iptv/languages/tam.m3u")
        assertTrue("URL without scheme should be rejected", result is ValidationResult.Invalid)
        assertEquals("URL must start with http:// or https://", (result as ValidationResult.Invalid).reason)
    }

    @Test
    fun testFtpOrInvalidSchemeRejected() {
        val result = PlaylistValidator.validateUrl("ftp://example.com/playlist.m3u")
        assertTrue("Non-HTTP URL should be rejected", result is ValidationResult.Invalid)
    }

    @Test
    fun testMissingHostRejected() {
        val result = PlaylistValidator.validateUrl("https://")
        assertTrue("URL without host should be rejected", result is ValidationResult.Invalid)
    }

    @Test
    fun testUrlNormalization() {
        val unpadded = "   https://example.com/list.m3u   \n"
        val normalized = PlaylistValidator.normalizeUrl(unpadded)
        assertEquals("https://example.com/list.m3u", normalized)
    }
}
