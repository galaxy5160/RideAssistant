package com.rideassistant.capture

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDetector @Inject constructor(
    private val registry: AppAdapterRegistry
) {
    private var allowlistedPackages: Set<String> = registry.getAllSupportedPackages()

    fun updateAllowlist(packages: Set<String>) {
        allowlistedPackages = packages
    }

    fun isAllowedPackage(packageName: String?): Boolean =
        packageName != null && allowlistedPackages.contains(packageName)

    fun detectPlatform(packageName: String?): String? =
        if (isAllowedPackage(packageName)) registry.getAdapterForPackage(packageName)?.platformName else null
}
