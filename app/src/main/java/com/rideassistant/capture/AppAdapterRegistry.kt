package com.rideassistant.capture

import com.rideassistant.capture.adapters.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAdapterRegistry @Inject constructor() {
    private val adapters: List<PlatformAdapter> = listOf(
        OlaAdapter(),
        UberAdapter(),
        RapidoAdapter(),
        PorterAdapter(),
        InDriveAdapter()
    )

    private val packageMap: Map<String, PlatformAdapter> = buildMap {
        adapters.forEach { adapter ->
            adapter.packageNames.forEach { pkg -> put(pkg, adapter) }
        }
    }

    fun getAdapterForPackage(packageName: String?): PlatformAdapter? =
        packageName?.let { packageMap[it] }

    fun getAllSupportedPackages(): Set<String> = packageMap.keys

    fun getAllAdapters(): List<PlatformAdapter> = adapters
}
