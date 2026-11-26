package com.rideassistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rideassistant.analytics.RideStatisticsSummary

@Composable
fun AnalyticsScreen(summary: RideStatisticsSummary) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Offline Analytics", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Total Rides: ${summary.rideCount}", style = MaterialTheme.typography.titleMedium)
                Text("Gross Earnings: ₹${summary.grossEarnings.toInt()}")
                Text("Estimated Net Profit: ₹${summary.estimatedNetProfit.toInt()}")
                Text("Average Fare: ₹${summary.averageFare.toInt()}")
                Text("Total Ride Distance: ${String.format("%.1f", summary.totalRideKm)} km")
                Text("Total Pickup Distance: ${String.format("%.1f", summary.totalPickupKm)} km")
                Text("Home Priority Matches: ${summary.homePriorityMatches}")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Platform Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (summary.platformCounts.isEmpty()) {
                    Text("No platform data yet.")
                } else {
                    summary.platformCounts.forEach { (platform, count) ->
                        Text("$platform: $count rides")
                    }
                }
            }
        }
    }
}
