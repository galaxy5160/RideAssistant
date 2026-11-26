package com.rideassistant

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point for RideAssistant.
 * Initializes Hilt dependency injection graph for offline-first driver assistance.
 */
@HiltAndroidApp
class RideAssistantApp : Application()
