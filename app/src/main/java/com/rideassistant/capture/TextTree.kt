package com.rideassistant.capture

import android.view.accessibility.AccessibilityNodeInfo

data class NodeMetadata(
    val viewId: String?,
    val className: String?,
    val depth: Int
)

data class RideOfferText(
    val platform: String,
    val detectedAt: Long,
    val visibleTexts: List<String>,
    val normalizedTexts: List<String>,
    val sourceNodeMetadata: List<NodeMetadata>,
    val confidence: Float
)

/**
 * Bounded, allocation-conscious TextTree extractor & normalizer.
 * Never retains AccessibilityNodeInfo references beyond extraction.
 */
object TextTree {
    private const val MAX_DEPTH = 14
    private const val MAX_NODES = 180

    private val MULTI_SPACE = Regex("\\s+")
    private val RUPEE_PREFIX = Regex("(?i)(rs\\.?|inr)\\s*")
    private val SENSITIVE_KEYWORDS = listOf("otp", "cvv", "pin", "password", "mpin")

    fun extractFromRoot(
        root: AccessibilityNodeInfo?,
        platform: String,
        detectedAt: Long = System.currentTimeMillis()
    ): RideOfferText {
        if (root == null) {
            return RideOfferText(platform, detectedAt, emptyList(), emptyList(), emptyList(), 0f)
        }

        val visible = ArrayList<String>(32)
        val normalized = ArrayList<String>(32)
        val metadata = ArrayList<NodeMetadata>(32)
        var visited = 0

        fun traverse(node: AccessibilityNodeInfo, depth: Int) {
            if (depth > MAX_DEPTH || visited >= MAX_NODES) return
            visited++

            val text = node.text?.toString()?.trim()
            val desc = node.contentDescription?.toString()?.trim()
            val raw = when {
                !text.isNullOrEmpty() -> text
                !desc.isNullOrEmpty() -> desc
                else -> null
            }

            if (raw != null && !node.isPassword && !containsSensitiveKeyword(raw)) {
                visible.add(raw)
                normalized.add(normalizeText(raw))
                metadata.add(
                    NodeMetadata(
                        viewId = node.viewIdResourceName,
                        className = node.className?.toString(),
                        depth = depth
                    )
                )
            }

            val childCount = node.childCount
            for (i in 0 until childCount) {
                if (visited >= MAX_NODES) break
                val child = node.getChild(i) ?: continue
                try {
                    traverse(child, depth + 1)
                } finally {
                    @Suppress("DEPRECATION")
                    child.recycle()
                }
            }
        }

        traverse(root, 0)
        val confidence = if (visible.isEmpty()) 0f else (visible.size.coerceAtMost(10) / 10f)
        return RideOfferText(
            platform = platform,
            detectedAt = detectedAt,
            visibleTexts = visible,
            normalizedTexts = normalized,
            sourceNodeMetadata = metadata,
            confidence = confidence
        )
    }

    fun normalizeText(input: String): String {
        var s = input.trim()
        s = MULTI_SPACE.replace(s, " ")
        s = RUPEE_PREFIX.replace(s, "₹")
        // Normalize Devanagari digits if present
        s = normalizeUnicodeDigits(s)
        return s
    }

    private fun normalizeUnicodeDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '\u0966'..'\u096F') {
                sb.append(('0'.code + (ch.code - '\u0966'.code)).toChar())
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun containsSensitiveKeyword(text: String): Boolean {
        val lower = text.lowercase()
        return SENSITIVE_KEYWORDS.any { lower.contains(it) }
    }
}
