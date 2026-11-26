package com.rideassistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "RideAssistant",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Master Monitoring Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (settings.monitoringEnabled) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (settings.monitoringEnabled) "Monitoring ACTIVE" else "Monitoring PAUSED",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Capture: ${captureState.status} • Mode: ${if (settings.homePriorityEnabled) "HOME PRIORITY" else "NORMAL"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Switch(
                    checked = settings.monitoringEnabled,
                    onCheckedChange = onToggleMonitoring
                )
            }
        }

        // Quick Toggles Row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Home Priority", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = homeLocation?.name ?: "Not Set",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = settings.homePriorityEnabled,
                        onCheckedChange = onToggleHomePriority
                    )
                }
            }
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Bubble", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = if (settings.bubbleEnabled) "ON" else "OFF",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = settings.bubbleEnabled,
                        onCheckedChange = onToggleBubble
                    )
                }
            }
        }

        // Recent Offer Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Recent Offer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (latestOffer == null) {
                    Text("No ride offer detected yet. Keep monitoring ON when using Ola/Uber/Rapido/Porter/inDrive.")
                } else {
                    val offer = latestOffer.offer
                    Text(
                        text = "${offer.platform} • ${offer.fare?.let { "₹${it.toInt()}" } ?: "₹?"} • ${latestOffer.score.category} (${latestOffer.score.totalScore}/100)",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Pickup: ${offer.pickupDistanceKm ?: "?"} km  |  Ride: ${offer.rideDistanceKm ?: "?"} km")
                    Text(
                        text = "HOME: ${latestOffer.homeDistance.displayLabel}",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Pipeline: ${latestOffer.pipelineLatencyMs} ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Today's Summary
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Offline Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Rides Logged: ${analytics.rideCount}  •  Gross: ₹${analytics.grossEarnings.toInt()}  •  Est. Net: ₹${analytics.estimatedNetProfit.toInt()}")
            }
        }

        // Quick Navigation Grid
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { onNavigate(NavRoutes.DIAGNOSTICS) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Diagnostics")
            }
            OutlinedButton(onClick = { onNavigate(NavRoutes.GUIDE) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Help, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Guide")
            }
            OutlinedButton(onClick = { onNavigate(NavRoutes.PRIVACY) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Privacy")
            }
        }
    }
}
