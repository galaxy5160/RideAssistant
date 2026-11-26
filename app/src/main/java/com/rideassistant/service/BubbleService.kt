package com.rideassistant.service

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.rideassistant.action.DriverAssistController
import com.rideassistant.action.NotificationController
import com.rideassistant.capture.CaptureManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class BubbleService : Service() {

    @Inject lateinit var driverAssistController: DriverAssistController
    @Inject lateinit var captureManager: CaptureManager

    private var windowManager: WindowManager? = null
    private var bubbleView: View? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isExpanded = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        if (Settings.canDrawOverlays(this)) {
            setupFloatingBubble()
            observeLatestOffer()
        } else {
            stopSelf()
        }
    }

    private fun startForegroundNotification() {
        val notification = NotificationCompat.Builder(this, NotificationController.CHANNEL_SERVICE)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("RideAssistant Bubble Active")
            .setContentText("Tap to manage floating ride assistant")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(2001, notification)
    }

    private fun setupFloatingBubble() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 240
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#E6161B22"))
            setPadding(24, 16, 24, 16)
        }

        val titleText = TextView(this).apply {
            text = "RA • IDLE"
            setTextColor(Color.parseColor("#00E676"))
            textSize = 14f
        }
        val detailText = TextView(this).apply {
            text = "Waiting for offer..."
            setTextColor(Color.WHITE)
            textSize = 12f
            visibility = View.GONE
        }

        container.addView(titleText)
        container.addView(detailText)

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (kotlin.math.abs(dx) < 10 && kotlin.math.abs(dy) < 10) {
                        isExpanded = !isExpanded
                        detailText.visibility = if (isExpanded) View.VISIBLE else View.GONE
                    }
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(container, params)
                    true
                }
                else -> false
            }
        }

        bubbleView = container
        windowManager?.addView(container, params)
    }

    private fun observeLatestOffer() {
        serviceScope.launch {
            driverAssistController.latestOffer.collect { analyzed ->
                val container = bubbleView as? LinearLayout ?: return@collect
                val titleText = container.getChildAt(0) as? TextView ?: return@collect
                val detailText = container.getChildAt(1) as? TextView ?: return@collect

                if (analyzed == null) {
                    titleText.text = "RA • IDLE"
                    detailText.text = "No offer detected yet"
                } else {
                    val fare = analyzed.offer.fare?.let { "₹${it.toInt()}" } ?: "₹?"
                    titleText.text = "$fare • ${analyzed.score.category} (${analyzed.score.totalScore})"
                    detailText.text = "${analyzed.offer.platform} • ${analyzed.homeDistance.displayLabel}"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        bubbleView?.let { windowManager?.removeView(it) }
        bubbleView = null
    }
}
