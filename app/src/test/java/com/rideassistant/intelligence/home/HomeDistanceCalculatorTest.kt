package com.rideassistant.intelligence.home

import org.junit.Assert.*
import org.junit.Test

class HomeDistanceCalculatorTest {

    @Test
    fun `haversine returns null for invalid or null coordinates`() {
        assertNull(HomeDistanceCalculator.calculateHaversineKm(null, 77.2, 28.6, 77.2))
        assertNull(HomeDistanceCalculator.calculateHaversineKm(95.0, 77.2, 28.6, 77.2))
        assertNull(HomeDistanceCalculator.calculateHaversineKm(28.6, 185.0, 28.6, 77.2))
    }

    @Test
    fun `haversine calculates accurate distance between Delhi points`() {
        val dist = HomeDistanceCalculator.calculateHaversineKm(28.6139, 77.2090, 28.5355, 77.3910)
        assertNotNull(dist)
        assertTrue(dist!! in 18.0..21.0)
    }

    @Test
    fun `formatDistance displays meters under 1km and km above 1km`() {
        val meters = HomeDistanceCalculator.formatDistance(0.65)
        assertTrue(meters.displayLabel.contains("650 m"))
        assertTrue(meters.displayLabel.contains("Straight-line"))

        val km = HomeDistanceCalculator.formatDistance(2.4)
        assertTrue(km.displayLabel.contains("2.4 km"))

        val unknown = HomeDistanceCalculator.formatDistance(null)
        assertEquals("UNKNOWN", unknown.displayLabel)
    }
}
