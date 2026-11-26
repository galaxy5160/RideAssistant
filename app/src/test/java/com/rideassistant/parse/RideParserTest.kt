package com.rideassistant.parse

import com.rideassistant.capture.RideOfferText
import com.rideassistant.capture.TextTree
import org.junit.Assert.*
import org.junit.Test

class RideParserTest {

    private val parser = RideParser(PatternStore())

    @Test
    fun `parses synthetic Uber offer text accurately`() {
        val raw = listOf("Uber Go", "₹185", "1.3 km away", "8.7 km", "22 min", "Connaught Place", "Noida Sector 18", "Accept")
        val offerText = RideOfferText(
            platform = "Uber",
            detectedAt = 1000L,
            visibleTexts = raw,
            normalizedTexts = raw.map { TextTree.normalizeText(it) },
            sourceNodeMetadata = emptyList(),
            confidence = 0.9f
        )

        val result = parser.parse(offerText)
        assertTrue(result is ParseResult.Success)
        val offer = (result as ParseResult.Success).offer
        assertEquals(185.0, offer.fare!!, 0.01)
        assertEquals(1.3, offer.pickupDistanceKm!!, 0.01)
        assertEquals(8.7, offer.rideDistanceKm!!, 0.01)
        assertEquals(22, offer.rideDurationMinutes)
        assertEquals("Go", offer.rideType)
    }

    @Test
    fun `returns Unknown when neither fare nor distance is present`() {
        val offerText = RideOfferText(
            platform = "Ola",
            detectedAt = 1000L,
            visibleTexts = listOf("Loading...", "Please wait"),
            normalizedTexts = listOf("Loading...", "Please wait"),
            sourceNodeMetadata = emptyList(),
            confidence = 0.2f
        )
        val result = parser.parse(offerText)
        assertTrue(result is ParseResult.Unknown)
    }
}
