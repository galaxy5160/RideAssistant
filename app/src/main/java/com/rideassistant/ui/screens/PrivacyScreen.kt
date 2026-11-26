package com.rideassistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyScreen(
    onClearHistory: () -> Unit,
    onClearHome: () -> Unit
) {
    var msg by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Privacy & Data Control", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "RideAssistant is 100% offline. No INTERNET permission is requested. No cloud AI, no telemetry, and no external servers are used."
        )
        Button(
            onClick = {
                onClearHistory()
                msg = "All local ride history deleted."
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete All Ride History")
        }
        OutlinedButton(
            onClick = {
                onClearHome()
                msg = "Saved HOME location deleted."
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete Saved HOME Location")
        }
        msg?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
    }
}
