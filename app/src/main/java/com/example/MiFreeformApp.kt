package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.util.LocaleHelper

class MiFreeformApp : Application() {

    override fun attachBaseContext(base: Context) {
        val updatedContext = LocaleHelper.applySavedLocale(base)
        super.attachBaseContext(updatedContext)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_OVERLAY,
                "Mi Freeform Dock Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground service notification for floating dock and window manager"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID_OVERLAY = "mi_freeform_overlay_channel"
        const val NOTIFICATION_ID_OVERLAY = 1001
    }
}
