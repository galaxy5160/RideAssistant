package com.rideassistant.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class OnboardingStepInfo(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isOptional: Boolean = false,
    val actionLabel: String? = null
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onSaveHomeLocation: (name: String, lat: Double, lon: Double) -> Unit = { _, _, _ -> },
    onSaveInitialMinFare: (Double) -> Unit = {}
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(0) }
    var homeName by remember { mutableStateOf("My Home") }
    var homeLatText by remember { mutableStateOf("28.6139") }
    var homeLonText by remember { mutableStateOf("77.2090") }
    var minFareText by remember { mutableStateOf("40") }

    val steps = remember {
        listOf(
            OnboardingStepInfo(
                1, "Welcome to RideAssistant",
                "Offline-first driver intelligence. Analyze ride offers in under 100 ms without cloud services or internet.",
                Icons.Default.DirectionsCar
            ),
            OnboardingStepInfo(
                2, "Supported Functionality",
                "Detects ride offers from Ola, Uber, Rapido, Porter & inDrive. Calculates fare/km, net profit, and distance to HOME. You always stay in control of accepting rides.",
                Icons.Default.Speed
            ),
            OnboardingStepInfo(
                3, "Accessibility Service",
                "Required to read visible ride offer cards on your screen. Event-driven only—never polls continuously, never captures OTPs or messages.",
                Icons.Default.AccessibilityNew,
                isOptional = true,
                actionLabel = "Open Accessibility Settings"
            ),
            OnboardingStepInfo(
                4, "Floating Overlay",
                "Optional floating bubble shows instant GOOD/CHECK/LOW score and Home Distance over ride apps.",
                Icons.Default.PictureInPictureAlt,
                isOptional = true,
                actionLabel = "Grant Overlay Permission"
            ),
            OnboardingStepInfo(
                5, "Notifications",
                "Fast heads-up alerts with fare, pickup distance, and Home Distance when an offer arrives.",
                Icons.Default.NotificationsActive,
                isOptional = true,
                actionLabel = "Open Notification Settings"
            ),
            OnboardingStepInfo(
                6, "Optional Location",
                "Used only locally to calculate straight-line distance from ride destinations to your saved HOME.",
                Icons.Default.MyLocation,
                isOptional = true
            ),
            OnboardingStepInfo(
                7, "100% Offline Privacy",
                "Zero cloud servers, zero external AI APIs, zero telemetry. All ride history, rules, and HOME coordinates stay strictly on your device.",
                Icons.Default.Lock
            ),
            OnboardingStepInfo(
                8, "Configure HOME Location",
                "Enter your HOME coordinates (or skip to set later) so RideAssistant can show how far each drop is from Home.",
                Icons.Default.Home,
                isOptional = true
            ),
            OnboardingStepInfo(
                9, "Configure Initial Rules",
                "Set your minimum acceptable fare (₹) to filter out low-paying offers.",
                Icons.Default.Tune,
                isOptional = true
            ),
            OnboardingStepInfo(
                10, "Ready & Diagnostics",
                "Setup complete! You can run a full system health check anytime from the Diagnostics tab.",
                Icons.Default.VerifiedUser
            )
        )
    }

    val step = steps[currentStep]

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                LinearProgressIndicator(
                    progress = { (currentStep + 1).toFloat() / steps.size },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Step ${step.stepNumber} of ${steps.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Icon(
                    imageVector = step.icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Step-specific interactive controls
                when (step.stepNumber) {
                    3 -> OutlinedButton(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(step.actionLabel ?: "Open Settings")
                    }
                    4 -> OutlinedButton(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(step.actionLabel ?: "Grant Overlay")
                    }
                    5 -> OutlinedButton(
                        onClick = {
                            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                            } else {
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(step.actionLabel ?: "Notification Settings")
                    }
                    8 -> {
                        OutlinedTextField(
                            value = homeName,
                            onValueChange = { homeName = it },
                            label = { Text("Home Label") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = homeLatText,
                                onValueChange = { homeLatText = it },
                                label = { Text("Latitude") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = homeLonText,
                                onValueChange = { homeLonText = it },
                                label = { Text("Longitude") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                    9 -> {
                        OutlinedTextField(
                            value = minFareText,
                            onValueChange = { minFareText = it },
                            label = { Text("Minimum Fare (₹)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    TextButton(onClick = { currentStep-- }) {
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(64.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (step.isOptional && currentStep < steps.lastIndex) {
                        TextButton(onClick = { currentStep++ }) {
                            Text("Skip")
                        }
                    }
                    Button(
                        onClick = {
                            if (step.stepNumber == 8) {
                                val lat = homeLatText.toDoubleOrNull()
                                val lon = homeLonText.toDoubleOrNull()
                                if (lat != null && lon != null) {
                                    onSaveHomeLocation(homeName.ifBlank { "Home" }, lat, lon)
                                }
                            }
                            if (step.stepNumber == 9) {
                                minFareText.toDoubleOrNull()?.let(onSaveInitialMinFare)
                            }
                            if (currentStep < steps.lastIndex) {
                                currentStep++
                            } else {
                                onComplete()
                            }
                        }
                    ) {
                        Text(if (currentStep == steps.lastIndex) "Finish & Launch" else "Next")
                    }
                }
            }
        }
    }
}
