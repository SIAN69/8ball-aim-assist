package com.sian69.aimassist

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class AimOverlayView(context: Context) : View(context) {

    // Settings (updated live from MainActivity)
    var lineColorHue: Int = 180        // 0-360 hue value (180 = cyan)
    var opacityPercent: Int = 80       // 0-100
    var lineLength: Int = 70           // 0-100 mapped to 200-1200px
    var showReflect: Boolean = true

    // Touch state
    private var ballX: Float = -1f
    private var ballY: Float = -1f
    private var aimX: Float = -1f
    private var aimY: Float = -1f
    private var ballPlaced: Boolean = false

    // Paints
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        pathEffect = DashPathEffect(floatArrayOf(26f, 12f), 0f)
    }
    private val ballPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val reflectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        pathEffect = DashPathEffect(floatArrayOf(18f, 10f), 0f)
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(200, 0, 255, 200)
        textSize = 36f
        typeface = Typeface.MONOSPACE
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!ballPlaced) {
                    // First touch = place cue ball
                    ballX = event.x
                    ballY = event.y
                    ballPlaced = true
                } else {
                    // Second touch = aim direction
                    aimX = event.x
                    aimY = event.y
                    invalidate()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (ballPlaced) {
                    aimX = event.x
                    aimY = event.y
                    invalidate()
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                // Two-finger tap = reset ball position
                ballPlaced = false
                ballX = -1f; ballY = -1f
                aimX = -1f; aimY = -1f
                invalidate()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val alpha = (opacityPercent / 100f * 255).toInt().coerceIn(0, 255)
        val lineColor = Color.HSVToColor(alpha, floatArrayOf(lineColorHue.toFloat(), 1f, 1f))
        val reflColor = Color.argb(alpha, 255, 120, 0)
        val actualLength = (200 + lineLength / 100f * 1000).toFloat()

        linePaint.color = lineColor
        linePaint.strokeWidth = 4f
        ballPaint.color = lineColor
        reflectPaint.color = reflColor
        reflectPaint.strokeWidth = 3f
        dotPaint.color = lineColor

        if (!ballPlaced) {
            // Show hint text
            canvas.drawText("👆 Tap to place cue ball", 40f, height / 2f, hintPaint)
            canvas.drawText("    Then drag to aim", 40f, height / 2f + 50f, hintPaint)
            canvas.drawText("✌ Two fingers = reset", 40f, height / 2f + 100f, hintPaint)
            return
        }

        // Draw ball ring
        canvas.drawCircle(ballX, ballY, 28f, ballPaint)

        if (aimX < 0) {
            canvas.drawText("👆 Now drag to aim", 40f, height / 2f, hintPaint)
            return
        }

        val dx = aimX - ballX
        val dy = aimY - ballY
        val dist = sqrt(dx * dx + dy * dy)
        if (dist < 5f) return

        val ux = dx / dist
        val uy = dy / dist

        // End point of aim line
        val ex = ballX + ux * actualLength
        val ey = ballY + uy * actualLength

        // Clamp to screen bounds
        val end = clampToScreen(ballX, ballY, ex, ey)

        // Draw main aim line
        canvas.drawLine(ballX, ballY, end.x, end.y, linePaint)

        // Small dot at aim point
        canvas.drawCircle(aimX, aimY, 8f, dotPaint)

        // Wall reflection
        if (showReflect) {
            val W = width.toFloat()
            val H = height.toFloat()
            val atLeft   = end.x <= 4f
            val atRight  = end.x >= W - 4f
            val atTop    = end.y <= 4f
            val atBottom = end.y >= H - 4f

            if (atLeft || atRight || atTop || atBottom) {
                val rux = if (atLeft || atRight) -ux else ux
                val ruy = if (atTop || atBottom) -uy else uy
                val rLen = actualLength * 0.55f
                val rx = end.x + rux * rLen
                val ry = end.y + ruy * rLen
                val rEnd = clampToScreen(end.x, end.y, rx, ry)
                canvas.drawLine(end.x, end.y, rEnd.x, rEnd.y, reflectPaint)
            }
        }
    }

    private fun clampToScreen(x1: Float, y1: Float, x2: Float, y2: Float): PointF {
        val W = width.toFloat()
        val H = height.toFloat()
        val dx = x2 - x1
        val dy = y2 - y1
        var t = 1f
        if (dx > 0) t = min(t, (W - x1) / dx)
        else if (dx < 0) t = min(t, -x1 / dx)
        if (dy > 0) t = min(t, (H - y1) / dy)
        else if (dy < 0) t = min(t, -y1 / dy)
        return PointF(x1 + dx * t, y1 + dy * t)
    }
}
