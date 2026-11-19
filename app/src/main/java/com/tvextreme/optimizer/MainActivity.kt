package com.tvextreme.optimizer

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.os.StatFs
import android.view.KeyEvent
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var panel: LinearLayout
    private lateinit var status: TextView
    private val navy = 0xFF0A101D.toInt()
    private val tile = 0xFF162033.toInt()
    private val blue = 0xFF2378FF.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = 5894 or 1024 or 512
        val scroll = ScrollView(this)
        panel = LinearLayout(this).apply { orientation = 1; setPadding(32, 24, 32, 32); setBackgroundColor(navy) }
        scroll.addView(panel); setContentView(scroll)
        panel.addView(text("TV EXTREME", 32, blue, true))
        panel.addView(text("ANDROID TV SYSTEM OPTIMIZER", 14, -1, true))
        panel.addView(text("Safe tools for a smoother TV experience", 16, 0xFFCBD5E1.toInt(), false))
        status = text("SYSTEM STATUS  •  Ready", 17, 0xFF6EDCA0.toInt(), true); panel.addView(status)
        action("⚡  PERFORMANCE MODE", "Enable a safe optimization session") {
            status.text = "PERFORMANCE MODE  •  Session active"
            Toast.makeText(this, "CPU/GPU clocks remain controlled by TV firmware.", Toast.LENGTH_LONG).show()
        }
        action("🧹  DEEP CLEANER", "Clear TV EXTREME temporary cache") {
            val bytes = cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
            cacheDir.deleteRecursively(); cacheDir.mkdirs()
            status.text = "CLEARED  •  ${fmt(bytes)} app cache"
        }
        action("📱  BACKGROUND APP MANAGER", "Open Android app-management settings") {
            try { startActivity(android.content.Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS)) }
            catch (_: Exception) { Toast.makeText(this, "System app settings unavailable", Toast.LENGTH_SHORT).show() }
        }
        action("📊  RAM & STORAGE MONITOR", "Show available memory and internal storage") { resources() }
        action("🌡  THERMAL MONITOR", "Read thermal status if Android exposes it") { thermal() }
        action("🚫  EXCLUSION LIST", "Keep essential system apps protected") {
            Toast.makeText(this, "Use system app settings for protected TV apps.", Toast.LENGTH_LONG).show()
        }
        panel.addView(text("IMPORTANT LIMITS", 18, blue, true))
        panel.addView(text("Android normally blocks apps from clearing other apps' private cache, force-stopping other apps, or setting CPU/GPU clocks. Those need supported OEM/system or ADB/Shizuku privileges. Thermal safety is never bypassed.", 14, 0xFFCBD5E1.toInt(), false))
    }

    private fun text(s: String, size: Int, color: Int, bold: Boolean) = TextView(this).apply {
        text = s; textSize = size.toFloat(); setTextColor(color); if (bold) setTypeface(null, 1); setPadding(8, 10, 8, 10)
    }
    private fun action(title: String, desc: String, run: () -> Unit) {
        val box = LinearLayout(this).apply {
            orientation = 1; setPadding(16, 10, 16, 10); setBackgroundColor(tile); isFocusable = true; isFocusableInTouchMode = true
            setOnClickListener { run() }
            setOnKeyListener { _, key, event ->
                if (event.action == KeyEvent.ACTION_UP && (key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER)) { run(); true } else false
            }
        }
        box.addView(text(title, 19, -1, true)); box.addView(text(desc, 13, 0xFFCBD5E1.toInt(), false))
        panel.addView(box, LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 8, 0, 8) })
    }
    private fun resources() {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo(); am.getMemoryInfo(mi)
        val fs = StatFs(filesDir.absolutePath)
        status.text = "RAM free ${fmt(mi.availMem)} / ${fmt(mi.totalMem)}  •  Storage free ${fmt(fs.availableBytes)} / ${fmt(fs.totalBytes)}"
    }
    private fun thermal() {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val pm = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            status.text = "THERMAL STATUS  •  " + when (pm.currentThermalStatus) {
                0 -> "None"; 1 -> "Light"; 2 -> "Moderate"; 3 -> "Severe"; 4 -> "Critical"; 5 -> "Emergency"; 6 -> "Shutdown"; else -> "Unknown"
            }
        } else status.text = "THERMAL STATUS  •  Not exposed by this Android version"
    }
    private fun fmt(n: Long): String = if (n < 1048576) "${n / 1024} KB" else String.format(Locale.US, "%.1f MB", n / 1048576.0)
}
