package com.rideassistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rideassistant.data.preferences.UserSettings

@Composable
fun RulesScreen(
    settings: UserSettings,
    onSaveRules: (minFare: Double, maxPickupKm: Double, minFarePerKm: Double, fuelCostPerKm: Double) -> Unit
) {
    var minFare by remember(settings) { mutableStateOf(settings.minFareRule.toString()) }
    var maxPickup by remember(settings) { mutableStateOf(settings.maxPickupKmRule.toString()) }
    var minFarePerKm by remember(settings) { mutableStateOf(settings.minFarePerKmRule.toString()) }
    var fuelCost by remember(settings) { mutableStateOf(settings.fuelCostPerKm.toString()) }
    var savedMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Driver Rules & Expenses", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = minFare,
            onValueChange = { minFare = it },
            label = { Text("Minimum Fare (₹)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = minFarePerKm,
            onValueChange = { minFarePerKm = it },
            label = { Text("Minimum Fare per km (₹/km)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = maxPickup,
            onValueChange = { maxPickup = it },
            label = { Text("Maximum Pickup Distance (km)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = fuelCost,
            onValueChange = { fuelCost = it },
            label = { Text("Fuel Cost per km (₹/km)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                val f = minFare.toDoubleOrNull() ?: settings.minFareRule
                val p = maxPickup.toDoubleOrNull() ?: settings.maxPickupKmRule
                val k = minFarePerKm.toDoubleOrNull() ?: settings.minFarePerKmRule
                val c = fuelCost.toDoubleOrNull() ?: settings.fuelCostPerKm
                onSaveRules(f, p, k, c)
                savedMessage = "Rules saved locally."
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Rules")
        }

        savedMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
    }
}
