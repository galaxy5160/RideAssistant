package com.rideassistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rideassistant.data.database.entities.RideHistoryEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    rides: List<RideHistoryEntity>,
    onDeleteRide: (String) -> Unit,
    onClearAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ride History", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (rides.isNotEmpty()) {
                TextButton(onClick = onClearAll) {
                    Text("Clear All")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (rides.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No rides recorded yet.")
            }
        } else {
            val fmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(rides, key = { it.id }) { ride ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${ride.platform} • ${ride.fare?.let { "₹${it.toInt()}" } ?: "₹?"} • ${ride.category} (${ride.score})",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Pickup: ${ride.pickupDistanceKm ?: "?"} km | Ride: ${ride.rideDistanceKm ?: "?"} km",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = fmt.format(Date(ride.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDeleteRide(ride.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
