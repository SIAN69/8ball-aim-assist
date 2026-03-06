package com.sian69.aimassist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val OVERLAY_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toggleBtn = findViewById<Button>(R.id.btnToggle)
        val statusText = findViewById<TextView>(R.id.tvStatus)
        val lineColorSeek = findViewById<SeekBar>(R.id.seekLineColor)
        val opacitySeek = findViewById<SeekBar>(R.id.seekOpacity)
        val lengthSeek = findViewById<SeekBar>(R.id.seekLength)
        val reflectSwitch = findViewById<Switch>(R.id.switchReflect)

        // Check overlay permission on start
        updateUI(statusText, toggleBtn)

        toggleBtn.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                // Request overlay permission
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, OVERLAY_PERMISSION_CODE)
            } else {
                // Toggle the overlay service
                if (OverlayService.isRunning) {
                    stopService(Intent(this, OverlayService::class.java))
                    statusText.text = "● Overlay OFF — tap to enable"
                    toggleBtn.text = "START AIM ASSIST"
                    toggleBtn.setBackgroundColor(getColor(R.color.colorStart))
                } else {
                    val serviceIntent = Intent(this, OverlayService::class.java).apply {
                        putExtra("lineColor", lineColorSeek.progress)
                        putExtra("opacity", opacitySeek.progress)
                        putExtra("length", lengthSeek.progress)
                        putExtra("reflect", reflectSwitch.isChecked)
                    }
                    startService(serviceIntent)
                    statusText.text = "● Overlay ON — open 8 Ball Pool now!"
                    toggleBtn.text = "STOP AIM ASSIST"
                    toggleBtn.setBackgroundColor(getColor(R.color.colorStop))
                }
            }
        }

        // Live update settings while running
        val seekListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                if (OverlayService.isRunning) {
                    OverlayService.instance?.updateSettings(
                        lineColorSeek.progress,
                        opacitySeek.progress,
                        lengthSeek.progress,
                        reflectSwitch.isChecked
                    )
                }
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        }
        lineColorSeek.setOnSeekBarChangeListener(seekListener)
        opacitySeek.setOnSeekBarChangeListener(seekListener)
        lengthSeek.setOnSeekBarChangeListener(seekListener)
        reflectSwitch.setOnCheckedChangeListener { _, checked ->
            if (OverlayService.isRunning) {
                OverlayService.instance?.updateSettings(
                    lineColorSeek.progress,
                    opacitySeek.progress,
                    lengthSeek.progress,
                    checked
                )
            }
        }
    }

    private fun updateUI(statusText: TextView, toggleBtn: Button) {
        if (!Settings.canDrawOverlays(this)) {
            statusText.text = "⚠ Permission needed — tap to grant"
            toggleBtn.text = "GRANT PERMISSION"
            toggleBtn.setBackgroundColor(getColor(R.color.colorWarn))
        } else if (OverlayService.isRunning) {
            statusText.text = "● Overlay ON — open 8 Ball Pool now!"
            toggleBtn.text = "STOP AIM ASSIST"
            toggleBtn.setBackgroundColor(getColor(R.color.colorStop))
        } else {
            statusText.text = "● Overlay OFF — tap to enable"
            toggleBtn.text = "START AIM ASSIST"
            toggleBtn.setBackgroundColor(getColor(R.color.colorStart))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_CODE) {
            val statusText = findViewById<TextView>(R.id.tvStatus)
            val toggleBtn = findViewById<Button>(R.id.btnToggle)
            updateUI(statusText, toggleBtn)
        }
    }

    override fun onResume() {
        super.onResume()
        val statusText = findViewById<TextView>(R.id.tvStatus)
        val toggleBtn = findViewById<Button>(R.id.btnToggle)
        updateUI(statusText, toggleBtn)
    }
}
