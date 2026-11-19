package com.tvextreme.optimizer

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.StatFs
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var panel: LinearLayout
    private lateinit var status: TextView
    private val actions = mutableListOf<LinearLayout>()
    private val titles = mutableListOf<TextView>()
    private val descriptions = mutableListOf<TextView>()
    private var selectedIndex = 0

    private val bgTop = 0xFF101B35.toInt()
    private val bgBottom = 0xFF070B16.toInt()
    private val glass = 0xCC18243B.toInt()
    private val glassSelected = 0xFF42D9FF.toInt()
    private val accent = 0xFF69E4FF.toInt()
    private val textMain = 0xFFF4F8FF.toInt()
    private val textMuted = 0xFFB9C9DE.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = 5894 or 1024 or 512

        val root = LinearLayout(this).apply {
            orientation = 1
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(bgTop, 0xFF0B1427.toInt(), bgBottom)
            )
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            descendantFocusability = android.view.ViewGroup.FOCUS_AFTER_DESCENDANTS
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        panel = LinearLayout(this).apply {
            orientation = 1
            setPadding(42, 30, 42, 36)
            isFocusable = false
        }
        scroll.addView(panel)
        root.addView(scroll, LinearLayout.LayoutParams(-1, -1))
        setContentView(root)

        panel.addView(text("TV EXTREME", 34, textMain, true))
        panel.addView(text("SMART TV  •  CONTROL CENTER", 13, accent, true))
        panel.addView(text("A cleaner, calmer way to manage your TV", 16, textMuted, false))

        status = text("●  SYSTEM READY  •  REMOTE READY", 15, 0xFF83E5B0.toInt(), true)
        status.setPadding(14, 14, 14, 20)
        panel.addView(status)

        action("⚡  PERFORMANCE MODE", "Start a safe optimization session") {
            status.text = "●  PERFORMANCE SESSION ACTIVE"
            Toast.makeText(this, "CPU/GPU clocks remain controlled by TV firmware.", Toast.LENGTH_LONG).show()
        }
        action("✦  DEEP CLEANER", "Clear TV EXTREME temporary cache") {
            val bytes = cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
            cacheDir.deleteRecursively()
            cacheDir.mkdirs()
            status.text = "●  CLEARED  •  ${fmt(bytes)} APP CACHE"
        }
        action("▣  APP MANAGER", "Open Android app-management settings") {
            try {
                startActivity(android.content.Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS))
            } catch (_: Exception) {
                Toast.makeText(this, "System app settings unavailable", Toast.LENGTH_SHORT).show()
            }
        }
        action("◫  RAM & STORAGE", "View available memory and internal storage") { resources() }
        action("♨  THERMAL MONITOR", "Read thermal status exposed by Android") { thermal() }
        action("⊘  EXCLUSION LIST", "Tips for protecting essential system apps") {
            Toast.makeText(this, "Use system app settings for protected TV apps.", Toast.LENGTH_LONG).show()
        }

        panel.addView(text("ABOUT SYSTEM ACCESS", 16, accent, true))
        panel.addView(text("Android limits access to other apps' private cache, force-stop controls and CPU/GPU clocks. These require supported system, OEM, root or ADB/Shizuku privileges. Thermal safety is never bypassed.", 13, textMuted, false))

        selectCard(0)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0 && actions.isNotEmpty()) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    selectCard((selectedIndex + 1).coerceAtMost(actions.lastIndex))
                    return true
                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    selectCard((selectedIndex - 1).coerceAtLeast(0))
                    return true
                }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                    actions[selectedIndex].performClick()
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun selectCard(index: Int) {
        if (actions.isEmpty()) return
        selectedIndex = index.coerceIn(0, actions.lastIndex)
        actions.forEachIndexed { i, card ->
            val chosen = i == selectedIndex
            card.background = cardBackground(chosen)
            card.scaleX = if (chosen) 1.025f else 1f
            card.scaleY = if (chosen) 1.025f else 1f
            card.elevation = if (chosen) 18f else 2f
            titles[i].setTextColor(if (chosen) 0xFF06172A.toInt() else textMain)
            descriptions[i].setTextColor(if (chosen) 0xFF12314A.toInt() else textMuted)
            card.contentDescription = if (chosen) "Selected: ${titles[i].text}" else titles[i].text
        }
        actions[selectedIndex].requestFocus()
    }

    private fun text(s: String, size: Int, color: Int, bold: Boolean) = TextView(this).apply {
        text = s
        textSize = size.toFloat()
        setTextColor(color)
        if (bold) setTypeface(null, 1)
        setPadding(8, 8, 8, 8)
        isFocusable = false
    }

    private fun cardBackground(focused: Boolean) = GradientDrawable(
        GradientDrawable.Orientation.TL_BR,
        if (focused) intArrayOf(0xFFFFFFFF.toInt(), 0xFF73E8FF.toInt(), 0xFF36BCEB.toInt())
        else intArrayOf(0xCC263A58.toInt(), glass)
    ).apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 26f
        setStroke(if (focused) 7 else 2, if (focused) 0xFFFFFFFF.toInt() else 0x667F9AB8)
    }

    private fun action(title: String, desc: String, run: () -> Unit) {
        val titleView = text(title, 19, textMain, true)
        val descView = text(desc, 13, textMuted, false)
        val box = LinearLayout(this).apply {
            orientation = 1
            setPadding(24, 18, 24, 18)
            background = cardBackground(false)
            isFocusable = true
            isFocusableInTouchMode = true
            isClickable = true
            setOnClickListener { run() }
            setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    val idx = actions.indexOf(view)
                    if (idx >= 0 && idx != selectedIndex) selectCard(idx)
                }
            }
        }
        box.addView(titleView)
        box.addView(descView)
        panel.addView(box, LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, 9, 0, 9)
        })
        actions.add(box)
        titles.add(titleView)
        descriptions.add(descView)
    }

    private fun resources() {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        val fs = StatFs(filesDir.absolutePath)
        status.text = "●  RAM FREE ${fmt(mi.availMem)} / ${fmt(mi.totalMem)}  •  STORAGE ${fmt(fs.availableBytes)} FREE"
    }

    private fun thermal() {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val pm = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            status.text = "●  THERMAL STATUS  •  " + when (pm.currentThermalStatus) {
                0 -> "None"; 1 -> "Light"; 2 -> "Moderate"; 3 -> "Severe"
                4 -> "Critical"; 5 -> "Emergency"; 6 -> "Shutdown"; else -> "Unknown"
            }
        } else status.text = "●  THERMAL STATUS  •  Not exposed"
    }

    private fun fmt(n: Long): String =
        if (n < 1048576) "${n / 1024} KB"
        else String.format(Locale.US, "%.1f MB", n / 1048576.0)
}