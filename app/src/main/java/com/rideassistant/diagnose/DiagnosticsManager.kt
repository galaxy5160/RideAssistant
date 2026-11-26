package com.rideassistant.diagnose

import android.Manifest
import android.accessibilityservice.AccessibilityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.rideassistant.capture.CaptureManager
import com.rideassistant.capture.CaptureStatus
import com.rideassistant.localai.LocalInferenceEngine
import com.rideassistant.parse.OCRFallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class HealthCheck(
    val name: String,
    val passed: Boolean,
    val statusText: String,
    val troubleshooting: String
)

@Singleton
class DiagnosticsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val captureManager: CaptureManager,
    private val ocrFallback: OCRFallback,
    private val localInferenceEngine: LocalInferenceEngine
) {
    fun runChecks(hasHomeLocation: Boolean): List<HealthCheck> {
        val accEnabled = isAccessibilityServiceEnabled()
        val overlayEnabled = Settings.canDrawOverlays(context)
        val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else true

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val ignoringBatteryOpt = powerManager.isIgnoringBatteryOptimizations(context.packageName)
        val captureStatus = captureManager.state.value.status

        return listOf(
            HealthCheck(
                name = "Accessibility Service",
                passed = accEnabled,
                statusText = if (accEnabled) "Enabled ($captureStatus)" else "Disabled",
                troubleshooting = "Enable RideAssistant Screen Reader in Android Settings -> Accessibility."
            ),
            HealthCheck(
                name = "Floating Overlay Permission",
                passed = overlayEnabled,
                statusText = if (overlayEnabled) "Granted" else "Not Granted",
                troubleshooting = "Grant 'Display over other apps' in App Settings for floating bubble."
            ),
            HealthCheck(
                name = "Notification Permission",
                passed = notifGranted,
                statusText = if (notifGranted) "Granted" else "Denied",
                troubleshooting = "Allow notifications so RideAssistant can show instant ride alerts."
            ),
            HealthCheck(
                name = "Battery Optimization",
                passed = ignoringBatteryOpt,
                statusText = if (ignoringBatteryOpt) "Unrestricted" else "Optimized (May sleep)",
                troubleshooting = "Set battery usage to Unrestricted if Android kills background capture."
            ),
            HealthCheck(
                name = "HOME Location",
                passed = hasHomeLocation,
                statusText = if (hasHomeLocation) "Configured (Local)" else "Not Set",
                troubleshooting = "Set your HOME coordinates in Home Priority tab to enable Home Distance."
            ),
            HealthCheck(
                name = "Offline OCR Fallback",
                passed = ocrFallback.isAvailable(),
                statusText = if (ocrFallback.isAvailable()) "Available" else "Not Installed (Optional)",
                troubleshooting = "Accessibility text extraction is active; offline OCR is optional."
            ),
            HealthCheck(
                name = "Local AI Engine",
                passed = localInferenceEngine.isModelLoaded(),
                statusText = if (localInferenceEngine.isModelLoaded()) "Loaded" else "Not Loaded (Optional)",
                troubleshooting = "Core scoring is deterministic and does not require a local AI model."
            )
        )
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(context.packageName) && am.isEnabled
    }
}
