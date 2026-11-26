package com.rideassistant.capture

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.rideassistant.rules.RideLog
import com.rideassistant.rules.RulesEngine

class RideAccessibilityService : AccessibilityService() {

    private val fareRegex = Regex("""(?:₹|Rs\.?\s*)(\d{2,5})""", RegexOption.IGNORE_CASE)
    private val kmRegex = Regex("""(\d+(?:\.\d+)?)\s*km""", RegexOption.IGNORE_CASE)

    override fun onServiceConnected() {
        super.onServiceConnected()
        RulesEngine.init(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !RulesEngine.rules.value.isEnabled) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return

        val root = rootInActiveWindow ?: return
        val texts = mutableListOf<String>()
        collectText(root, texts)
        val joined = texts.joinToString(" | ")

        val fare = fareRegex.find(joined)?.groupValues?.getOrNull(1)?.toIntOrNull()
        val km = kmRegex.find(joined)?.groupValues?.getOrNull(1)?.toDoubleOrNull()

        if (fare != null) {
            val matched = RulesEngine.evaluate(fare, km)
            var action = if (matched) "Matched" else "Skipped"
            if (matched && RulesEngine.rules.value.autoAccept) {
                if (tryClickAccept(root)) {
                    action = "Auto-Accepted"
                }
            }
            RulesEngine.addLog(
                RideLog(
                    timestamp = System.currentTimeMillis(),
                    sourceApp = pkg,
                    fare = fare,
                    distanceKm = km,
                    matched = matched,
                    actionTaken = action
                )
            )
        }
    }

    private fun collectText(node: AccessibilityNodeInfo?, out: MutableList<String>) {
        if (node == null) return
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let { out.add(it) }
        for (i in 0 until node.childCount) {
            collectText(node.getChild(i), out)
        }
    }

    private fun tryClickAccept(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        val txt = node.text?.toString()?.lowercase() ?: ""
        if (node.isClickable && (txt.contains("accept") || txt.contains("confirm"))) {
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
        for (i in 0 until node.childCount) {
            if (tryClickAccept(node.getChild(i))) return true
        }
        return false
    }

    override fun onInterrupt() {}
}
