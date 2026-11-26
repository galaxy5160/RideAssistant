package com.rideassistant.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import com.rideassistant.rules.RulesEngine

class BubbleService : Service() {

    private var windowManager: WindowManager? = null
    private var bubbleView: TextView? = null

    override fun onCreate() {
        super.onCreate()
        RulesEngine.init(applicationContext)
        startForeground(101, createNotification())
        showBubble()
    }

    private fun createNotification(): Notification {
        val channelId = "ride_assistant_fg"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "RideAssistant Service", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
        return Notification.Builder(this, channelId)
            .setContentTitle("RideAssistant Active")
            .setContentText("Monitoring ride offers in background")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()
    }

    private fun showBubble() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 250
        }

        val tv = TextView(this).apply {
            setPadding(32, 20, 32, 20)
            setTextColor(Color.WHITE)
            textSize = 14f
            updateBubbleStyle(this)
        }

        var initialX = 0
        var initialY = 0
        var touchX = 0f
        var touchY = 0f
        var moved = false

        tv.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - touchX).toInt()
                    val dy = (event.rawY - touchY).toInt()
                    if (kotlin.math.abs(dx) > 8 || kotlin.math.abs(dy) > 8) moved = true
                    params.x = initialX + dx
                    params.y = initialY + dy
                    windowManager?.updateViewLayout(tv, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) {
                        val cur = RulesEngine.rules.value
                        RulesEngine.updateRules(applicationContext, cur.copy(isEnabled = !cur.isEnabled))
                        updateBubbleStyle(tv)
                    }
                    true
                }
                else -> false
            }
        }

        bubbleView = tv
        windowManager?.addView(tv, params)
    }

    private fun updateBubbleStyle(tv: TextView) {
        val enabled = RulesEngine.rules.value.isEnabled
        tv.text = if (enabled) "RIDE: ON" else "RIDE: OFF"
        tv.setBackgroundColor(if (enabled) Color.parseColor("#1B8A3E") else Color.parseColor("#B3261E"))
    }

    override fun onDestroy() {
        bubbleView?.let { windowManager?.removeView(it) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
