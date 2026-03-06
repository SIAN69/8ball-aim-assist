package com.sian69.aimassist

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.IBinder
import android.view.*
import androidx.core.app.NotificationCompat

class OverlayService : Service() {

    companion object {
        var isRunning = false
        var instance: OverlayService? = null
    }

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: AimOverlayView
    private val CHANNEL_ID = "8bp_aim_assist"

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        instance = this
        createNotificationChannel()
        startForeground(1, buildNotification())

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Create the transparent overlay view
        overlayView = AimOverlayView(this)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START

        windowManager.addView(overlayView, params)
    }

    fun updateSettings(lineColorHue: Int, opacity: Int, length: Int, reflect: Boolean) {
        overlayView.lineColorHue = lineColorHue
        overlayView.opacityPercent = opacity
        overlayView.lineLength = length
        overlayView.showReflect = reflect
        overlayView.invalidate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            overlayView.lineColorHue  = it.getIntExtra("lineColor", 180)
            overlayView.opacityPercent = it.getIntExtra("opacity", 80)
            overlayView.lineLength    = it.getIntExtra("length", 70)
            overlayView.showReflect   = it.getBooleanExtra("reflect", true)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        instance = null
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "8BP Aim Assist",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Aim assist overlay is active" }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, OverlayService::class.java)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("8BP Aim Assist")
            .setContentText("Overlay active — open 8 Ball Pool to use")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
