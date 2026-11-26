package com.rideassistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GuideScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Driver Guide & Help", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        GuideItem("How Accessibility Works", "RideAssistant listens only to allowlisted ride-hailing apps (Ola, Uber, Rapido, Porter, inDrive) when a screen changes. It never polls continuously and never reads OTPs or messages.")
        GuideItem("How Home Distance Works", "Using your locally saved HOME coordinates, RideAssistant calculates straight-line Haversine distance from the ride destination to HOME.")
        GuideItem("Troubleshooting Battery", "If Android stops background monitoring, set RideAssistant battery usage to Unrestricted in Android App Info.")
    }
}

@Composable
private fun GuideItem(title: String, body: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
