package com.rideassistant.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val version: String,
    val notes: String,
    val downloadUrl: String
)

object GitHubUpdateChecker {
    private const val RELEASES_API =
        "https://api.github.com/repos/galaxy5160/RideAssistant/releases/latest"

    suspend fun checkForUpdate(currentVersion: String): AppUpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val conn = (URL(RELEASES_API).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Accept", "application/vnd.github+json")
            }
            if (conn.responseCode != 200) return@withContext null

            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val tag = json.optString("tag_name").removePrefix("v").trim()
            if (tag.isBlank() || !isNewer(tag, currentVersion)) return@withContext null

            val notes = json.optString("body", "New update available on GitHub.")
            val assets = json.optJSONArray("assets")
            var apkUrl = json.optString("html_url")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.optString("browser_download_url", apkUrl)
                        break
                    }
                }
            }
            AppUpdateInfo(version = tag, notes = notes, downloadUrl = apkUrl)
        } catch (_: Exception) {
            null
        }
    }

    private fun isNewer(remote: String, current: String): Boolean {
        val rParts = remote.split(".").mapNotNull { it.toIntOrNull() }
        val cParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(rParts.size, cParts.size)
        for (i in 0 until maxLen) {
            val r = rParts.getOrElse(i) { 0 }
            val c = cParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}
