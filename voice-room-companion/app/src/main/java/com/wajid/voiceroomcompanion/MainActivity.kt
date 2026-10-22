package com.wajid.voiceroomcompanion

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("events", Context.MODE_PRIVATE) }
    private lateinit var historyBox: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        requestNotificationPermissionIfNeeded()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 28, 24, 24)
            setBackgroundColor(0xFFF5F7FB.toInt())
        }
        root.addView(TextView(this).apply {
            text = "Voice Room Companion"
            textSize = 25f
            setTextColor(0xFF14213D.toInt())
            setPadding(0, 0, 0, 8)
        })
        root.addView(TextView(this).apply {
            text = "imo Voice Room-এর জন্য নিরাপদ সহায়ক"
            textSize = 15f
            setTextColor(0xFF4C566A.toInt())
        })
        root.addView(TextView(this).apply {
            text = "নোট: Kick স্বয়ংক্রিয়ভাবে শনাক্ত বা প্রতিরোধ করা যায় না। নিচের বোতামগুলো আপনার দেখা ঘটনাগুলো লগ করে।"
            textSize = 14f
            setTextColor(0xFF8A3B12.toInt())
            setPadding(0, 16, 0, 12)
        })

        root.addView(makeButton("এক ট্যাপে imo খুলুন") { openImo() })
        root.addView(makeButton("Kick / Remove হয়েছে — লগ করুন") {
            logEvent("Kick / Remove")
        })
        root.addView(makeButton("রুম থেকে Disconnect হয়েছে — লগ করুন") {
            logEvent("Disconnect")
        })
        root.addView(makeButton("History মুছে ফেলুন") {
            AlertDialog.Builder(this)
                .setTitle("History মুছবেন?")
                .setMessage("এই ফোনে সংরক্ষিত সব ইভেন্ট মুছে যাবে।")
                .setNegativeButton("বাতিল", null)
                .setPositiveButton("মুছুন") { _, _ ->
                    prefs.edit().remove("items").apply()
                    refreshHistory()
                }.show()
        })

        root.addView(TextView(this).apply {
            text = "ঘটনার History"
            textSize = 20f
            setTextColor(0xFF14213D.toInt())
            setPadding(0, 20, 0, 8)
        })
        val scroll = ScrollView(this)
        historyBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(historyBox)
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        setContentView(root)
        refreshHistory()
    }

    private fun makeButton(label: String, action: () -> Unit): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 6 }
        }

    private fun openImo() {
        val candidates = listOf("com.imo.android", "com.imo.android.imoim")
        val launchIntent = candidates.firstNotNullOfOrNull { pkg ->
            try { packageManager.getLaunchIntentForPackage(pkg) } catch (_: Exception) { null }
        }
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            AlertDialog.Builder(this)
                .setTitle("imo পাওয়া যায়নি")
                .setMessage("imo ইনস্টল আছে কি না দেখুন। আপনার imo সংস্করণের package name আলাদা হতে পারে।")
                .setPositiveButton("ঠিক আছে", null)
                .show()
        }
    }

    private fun logEvent(type: String) {
        val items = loadEvents()
        items.put(0, JSONObject().apply {
            put("type", type)
            put("time", System.currentTimeMillis())
        })
        while (items.length() > 100) items.remove(items.length() - 1)
        prefs.edit().putString("items", items.toString()).apply()
        sendEventNotification(type)
        refreshHistory()
        Toast.makeText(this, "ঘটনা লগ করা হয়েছে", Toast.LENGTH_SHORT).show()
    }

    private fun sendEventNotification(type: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val notification = android.app.Notification.Builder(this, "events")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Voice Room Companion")
            .setContentText("আপনি লগ করেছেন: $type")
            .setAutoCancel(true)
            .build()
        getSystemService(NotificationManager::class.java)
            .notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel("events", "Voice Room Events", NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { description = "Local event log notifications" }
            )
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }
    }

    private fun loadEvents(): JSONArray = try {
        JSONArray(prefs.getString("items", "[]"))
    } catch (_: Exception) { JSONArray() }

    private fun refreshHistory() {
        historyBox.removeAllViews()
        val items = loadEvents()
        if (items.length() == 0) {
            historyBox.addView(TextView(this).apply {
                text = "এখনো কোনো ঘটনা লগ করা হয়নি।"
                textSize = 14f
                setPadding(0, 8, 0, 16)
            })
            return
        }
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        for (i in 0 until items.length()) {
            val event = items.optJSONObject(i) ?: continue
            val line = TextView(this).apply {
                text = "• ${event.optString("type")}\n  ${formatter.format(Date(event.optLong("time")))}"
                textSize = 14f
                setTextColor(0xFF202938.toInt())
                setPadding(14, 12, 14, 12)
                setBackgroundColor(0xFFFFFFFF.toInt())
            }
            historyBox.addView(line, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8 })
        }
    }
}
