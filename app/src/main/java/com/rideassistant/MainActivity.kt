package com.rideassistant

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rideassistant.rules.RideLog
import com.rideassistant.rules.RulesEngine
import com.rideassistant.ui.BubbleService
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RulesEngine.init(applicationContext)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF00E676),
                    onPrimary = Color(0xFF003919),
                    surface = Color(0xFF12151A),
                    background = Color(0xFF0B0D10),
                    surfaceVariant = Color(0xFF1C2128),
                    onSurface = Color(0xFFF0F4F8),
                    onSurfaceVariant = Color(0xFF9AA4B2)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RideAssistantMainUI()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideAssistantMainUI() {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("RideAssistant", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Rounded.PowerSettingsNew, contentDescription = null) },
                    label = { Text("Status") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Rounded.Tune, contentDescription = null) },
                    label = { Text("Rules") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Rounded.History, contentDescription = null) },
                    label = { Text("History") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when (selectedTab) {
                0 -> StatusScreen()
                1 -> RulesScreen()
                2 -> HistoryScreen()
            }
        }
    }
}

@Composable
fun StatusScreen() {
    val context = LocalContext.current
    val rules by RulesEngine.rules.collectAsState()
    val statusColor = if (rules.isEnabled) Color(0xFF00E676) else Color(0xFFFF5252)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (rules.isEnabled) "Assistant Active" else "Assistant Paused",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (rules.isEnabled) "Monitoring ride offers" else "Tap switch to start",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = rules.isEnabled,
                    onCheckedChange = { RulesEngine.updateRules(context, rules.copy(isEnabled = it)) }
                )
            }
        }

        Text(
            "Quick Setup",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ActionCard(
            icon = Icons.Rounded.AccessibilityNew,
            title = "1. Accessibility Service",
            subtitle = "Allow reading ride offer popups",
            onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        )

        ActionCard(
            icon = Icons.Rounded.Layers,
            title = "2. Overlay Permission",
            subtitle = "Allow floating ON/OFF bubble",
            onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                )
            }
        )

        ActionCard(
            icon = Icons.Rounded.BubbleChart,
            title = "3. Start Floating Bubble",
            subtitle = "Keep assistant running in background",
            onClick = {
                context.startForegroundService(Intent(context, BubbleService::class.java))
                Toast.makeText(context, "Floating bubble started", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun ActionCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun RulesScreen() {
    val context = LocalContext.current
    val rules by RulesEngine.rules.collectAsState()
    var minFare by remember(rules.minFare) { mutableStateOf(rules.minFare.toString()) }
    var maxKm by remember(rules.maxPickupKm) { mutableStateOf(rules.maxPickupKm.toString()) }
    var minRate by remember(rules.minPerKmRate) { mutableStateOf(rules.minPerKmRate.toString()) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = minFare,
                    onValueChange = { minFare = it },
                    label = { Text("Minimum Fare (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = maxKm,
                    onValueChange = { maxKm = it },
                    label = { Text("Max Distance (km)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = minRate,
                    onValueChange = { minRate = it },
                    label = { Text("Minimum ₹ / km") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Accept Rides", fontWeight = FontWeight.SemiBold)
                        Text("Automatically tap Accept on match", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = rules.autoAccept,
                        onCheckedChange = { RulesEngine.updateRules(context, rules.copy(autoAccept = it)) }
                    )
                }
            }
        }

        Button(
            onClick = {
                RulesEngine.updateRules(
                    context,
                    rules.copy(
                        minFare = minFare.toIntOrNull() ?: 50,
                        maxPickupKm = maxKm.toDoubleOrNull() ?: 3.0,
                        minPerKmRate = minRate.toDoubleOrNull() ?: 10.0
                    )
                )
                Toast.makeText(context, "Rules saved!", Toast.LENGTH_SHORT).show()
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Save Rules", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HistoryScreen() {
    val logs by RulesEngine.logs.collectAsState()
    if (logs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text("No rides captured yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(logs) { log ->
                HistoryCard(log)
            }
        }
    }
}

@Composable
fun HistoryCard(log: RideLog) {
    val badgeColor = when (log.actionTaken) {
        "Auto-Accepted" -> Color(0xFF00E676)
        "Matched" -> Color(0xFF40C4FF)
        else -> Color(0xFF9AA4B2)
    }
    val timeStr = remember(log.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("₹${log.fare ?: "-"}  •  ${log.distanceKm ?: "-"} km", fontWeight = FontWeight.Bold)
                Text("${log.sourceApp}  •  $timeStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeColor.copy(alpha = 0.18f)
            ) {
                Text(
                    log.actionTaken,
                    color = badgeColor,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}
