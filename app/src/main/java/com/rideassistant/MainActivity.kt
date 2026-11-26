package com.rideassistant

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rideassistant.rules.RulesEngine
import com.rideassistant.ui.BubbleService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RulesEngine.init(applicationContext)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RideAssistantApp()
                }
            }
        }
    }
}

@Composable
fun RideAssistantApp() {
    val context = LocalContext.current
    val rules by RulesEngine.rules.collectAsState()
    val logs by RulesEngine.logs.collectAsState()
    var tab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("RideAssistant (No Login)", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Status") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Rules") })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("History") })
        }

        Spacer(Modifier.height(16.dp))

        when (tab) {
            0 -> {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Assistant Active", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    Switch(
                        checked = rules.isEnabled,
                        onCheckedChange = { RulesEngine.updateRules(context, rules.copy(isEnabled = it)) }
                    )
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("1. Enable Accessibility Service") }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("2. Grant Overlay Permission") }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { context.startForegroundService(Intent(context, BubbleService::class.java)) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("3. Start Floating Bubble") }
            }
            1 -> {
                var minFare by remember(rules.minFare) { mutableStateOf(rules.minFare.toString()) }
                var maxKm by remember(rules.maxPickupKm) { mutableStateOf(rules.maxPickupKm.toString()) }

                OutlinedTextField(
                    value = minFare,
                    onValueChange = { minFare = it },
                    label = { Text("Minimum Fare (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = maxKm,
                    onValueChange = { maxKm = it },
                    label = { Text("Max Distance (km)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto-Accept Matching Rides", modifier = Modifier.weight(1f))
                    Switch(
                        checked = rules.autoAccept,
                        onCheckedChange = { RulesEngine.updateRules(context, rules.copy(autoAccept = it)) }
                    )
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        RulesEngine.updateRules(
                            context,
                            rules.copy(
                                minFare = minFare.toIntOrNull() ?: 50,
                                maxPickupKm = maxKm.toDoubleOrNull() ?: 3.0
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save Rules") }
            }
            2 -> {
                LazyColumn {
                    items(logs) { log ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("${log.actionTaken} | ₹${log.fare ?: "-"} | ${log.distanceKm ?: "-"} km")
                                Text(log.sourceApp, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
