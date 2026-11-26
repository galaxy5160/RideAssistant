package com.rideassistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rideassistant.action.AnalyzedOfferState
import com.rideassistant.analytics.RideStatisticsSummary
import com.rideassistant.capture.CaptureState
import com.rideassistant.data.database.entities.HomeLocationEntity
import com.rideassistant.data.preferences.UserSettings
import com.rideassistant.ui.navigation.NavRoutes

@Composable
fun HomeScreen(
    settings: UserSettings,
    captureState: CaptureState,
    latestOffer: AnalyzedOfferState?,
    homeLocation: HomeLocationEntity?,
    analytics: RideStatisticsSummary,
    onToggleMonitoring: (Boolean) -> Unit,
    onToggleHomePriority: (Boolean) -> Unit,
    onToggleBubble: (Boolean) -> Unit,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Minimal Header + Master Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "RideAssistant",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (settings.monitoringEnabled) "Active • ${captureState.status}" else "Paused",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = settings.monitoringEnabled,
                onCheckedChange = onToggleMonitoring
            )
        }

        // Minimal Mode Chips
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = settings.homePriorityEnabled,
                onClick = { onToggleHomePriority(!settings.homePriorityEnabled) },
                label = { Text("Home Priority") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = settings.bubbleEnabled,
                onClick = { onToggleBubble(!settings.bubbleEnabled) },
                label = { Text("Floating Bubble") },
                modifier = Modifier.weight(1f)
            )
        }

        // Clean Recent Offer Card
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (latestOffer == null) {
                    Text(
                        text = "Waiting for ride offer...",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val offer = latestOffer.offer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = offer.fare?.let { "₹${it.toInt()}" } ?: "₹?",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${latestOffer.score.category} • ${latestOffer.score.totalScore}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "${offer.platform}  •  Pickup ${offer.pickupDistanceKm ?: "?"} km  •  Ride ${offer.rideDistanceKm ?: "?"} km",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "HOME: ${latestOffer.homeDistance.displayLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Subtle Footer Links
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = { onNavigate(NavRoutes.DIAGNOSTICS) }) { Text("Diagnostics") }
            TextButton(onClick = { onNavigate(NavRoutes.GUIDE) }) { Text("Guide") }
            TextButton(onClick = { onNavigate(NavRoutes.PRIVACY) }) { Text("Privacy") }
        }
    }
}
