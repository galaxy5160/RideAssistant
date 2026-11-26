package com.rideassistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rideassistant.data.database.entities.HomeLocationEntity

@Composable
fun HomePriorityScreen(
    homePriorityEnabled: Boolean,
    homeLocation: HomeLocationEntity?,
    onToggleHomePriority: (Boolean) -> Unit,
    onSaveHome: (name: String, lat: Double, lon: Double, radiusKm: Double) -> Unit
) {
    var name by remember(homeLocation) { mutableStateOf(homeLocation?.name ?: "My Home") }
    var lat by remember(homeLocation) { mutableStateOf(homeLocation?.latitude?.toString() ?: "") }
    var lon by remember(homeLocation) { mutableStateOf(homeLocation?.longitude?.toString() ?: "") }
    var radius by remember(homeLocation) { mutableStateOf(homeLocation?.radiusKm?.toString() ?: "5.0") }
    var statusMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Home Priority Mode", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Home Priority", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Prioritize rides heading toward your saved HOME.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = homePriorityEnabled, onCheckedChange = onToggleHomePriority)
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Home Label") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = lat,
            onValueChange = { lat = it },
            label = { Text("Latitude (e.g. 28.6139)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = lon,
            onValueChange = { lon = it },
            label = { Text("Longitude (e.g. 77.2090)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = radius,
            onValueChange = { radius = it },
            label = { Text("Home Radius (km)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                val latVal = lat.toDoubleOrNull()
                val lonVal = lon.toDoubleOrNull()
                val radVal = radius.toDoubleOrNull() ?: 5.0
                if (latVal != null && lonVal != null && latVal in -90.0..90.0 && lonVal in -180.0..180.0) {
                    onSaveHome(name.ifBlank { "Home" }, latVal, lonVal, radVal)
                    statusMsg = "HOME coordinates saved locally."
                } else {
                    statusMsg = "Invalid coordinates. Please enter valid latitude & longitude."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save HOME Location")
        }

        statusMsg?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

        Text(
            text = "Privacy: Your HOME coordinates are stored strictly on-device in Room and never leave your phone.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
