package com.rideassistant.capture.adapters

import com.rideassistant.capture.RideOfferText

interface PlatformAdapter {
    val platformName: String
    val packageNames: Set<String>
    val offerKeywords: List<String>

    fun isLikelyOfferScreen(offerText: RideOfferText): Boolean {
        if (offerText.normalizedTexts.isEmpty()) return false
        val joined = offerText.normalizedTexts.joinToString(" ").lowercase()
        val hasCurrencyOrDistance = joined.contains("₹") || joined.contains("km")
        val hasKeyword = offerKeywords.any { joined.contains(it) }
        return hasCurrencyOrDistance && hasKeyword
    }
}

class OlaAdapter : PlatformAdapter {
    override val platformName = "Ola"
    override val packageNames = setOf("com.olacabs.oladriver", "com.olacabs.customer")
    override val offerKeywords = listOf("accept", "pickup", "drop", "km", "min", "ola")
}

class UberAdapter : PlatformAdapter {
    override val platformName = "Uber"
    override val packageNames = setOf("com.ubercab.driver", "com.ubercab")
    override val offerKeywords = listOf("accept", "pickup", "min", "km", "uber", "trip")
}

class RapidoAdapter : PlatformAdapter {
    override val platformName = "Rapido"
    override val packageNames = setOf("com.rapido.rider", "com.rapido.captain")
    override val offerKeywords = listOf("accept", "pickup", "drop", "km", "rapido", "order")
}

class PorterAdapter : PlatformAdapter {
    override val platformName = "Porter"
    override val packageNames = setOf("com.theporter.android.driverapp", "com.theporter.android.customerapp")
    override val offerKeywords = listOf("accept", "pickup", "drop", "km", "porter", "trip")
}

class InDriveAdapter : PlatformAdapter {
    override val platformName = "inDrive"
    override val packageNames = setOf("sinet.startup.inDriver")
    override val offerKeywords = listOf("accept", "offer", "km", "min", "indrive")
}
