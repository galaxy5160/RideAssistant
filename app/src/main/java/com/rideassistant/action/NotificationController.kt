package com.rideassistant.action

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ALERTS = "ride_offers_alerts"
        const val CHANNEL_SERVICE = "ride_assistant_service"
        private const val ALERT_NOTIFICATION_ID = 1001
    }

    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alertChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "Ride Offer Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Instant ride offer analysis and Home Distance alerts"
            }
            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE,
                "RideAssistant Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps floating bubble active when enabled"
            }
            manager.createNotificationChannel(alertChannel)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    fun showOfferAlert(
        platform: String,
        fare: Double?,
        pickupKm: Double?,
        rideKm: Double?,
        homeDistanceLabel: String,
        score: Int,
        category: String,
        isHomePriority: Boolean
    ) {
        val fareText = fare?.let { "₹${it.toInt()}" } ?: "₹?"
        val title = if (isHomePriority) {
            "RideAssistant • $fareText • HOME PRIORITY"
        } else {
            "RideAssistant • $platform • $fareText ($category)"
        }

        val body = buildString {
            pickupKm?.let { append("Pickup: ${it} km  ") }
            rideKm?.let { append("Ride: ${it} km\n") }
            append("Home: $homeDistanceLabel • Score: $score/100")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            manager.notify(ALERT_NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted
        }
    }
}
