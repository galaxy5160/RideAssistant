package com.rideassistant.capture

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Event-driven AccessibilityService for detecting ride offers on allowlisted platforms.
 * Never polls continuously and never retains AccessibilityNodeInfo references.
 */
@AndroidEntryPoint
class RideAccessibilityService : AccessibilityService() {

    @Inject lateinit var adapterRegistry: AppAdapterRegistry
    @Inject lateinit var appDetector: AppDetector
    @Inject lateinit var captureManager: CaptureManager

    override fun onServiceConnected() {
        super.onServiceConnected()
        captureManager.onServiceConnected()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return
        if (!appDetector.isAllowedPackage(pkg)) return

        val adapter = adapterRegistry.getAdapterForPackage(pkg) ?: return
        val startNs = System.nanoTime()

        val root = rootInActiveWindow ?: return
        try {
            val offerText = TextTree.extractFromRoot(root, adapter.platformName)
            if (adapter.isLikelyOfferScreen(offerText)) {
                val durationMs = (System.nanoTime() - startNs) / 1_000_000L
                captureManager.onOfferCaptured(offerText, durationMs)
            }
        } catch (e: Exception) {
            captureManager.onError(e.message ?: "Accessibility extraction error")
        } finally {
            @Suppress("DEPRECATION")
            root.recycle()
        }
    }

    override fun onInterrupt() {
        captureManager.onServiceDisconnected("Accessibility service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        captureManager.onServiceDisconnected("Accessibility service destroyed")
    }
}
