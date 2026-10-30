package com.example.util

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast
import com.example.model.WindowPreset

object FreeformLauncherHelper {

    fun isAndroid15OrHigher(): Boolean {
        return Build.VERSION.SDK_INT >= 35
    }

    fun isFreeformSupportedInSystem(context: Context): Boolean {
        return try {
            val freeformSupport = Settings.Global.getInt(
                context.contentResolver,
                "enable_freeform_support",
                0
            )
            freeformSupport == 1
        } catch (e: Exception) {
            false
        }
    }

    fun isForceResizableEnabled(context: Context): Boolean {
        return try {
            val resizable = Settings.Global.getInt(
                context.contentResolver,
                "force_resizable_activities",
                0
            )
            resizable == 1
        } catch (e: Exception) {
            false
        }
    }

    fun calculateLaunchBounds(context: Context, preset: WindowPreset): Rect {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val currentBounds = windowManager.currentWindowMetrics.bounds
            displayMetrics.widthPixels = currentBounds.width()
            displayMetrics.heightPixels = currentBounds.height()
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        }

        val screenWidth = displayMetrics.widthPixels.coerceAtLeast(720)
        val screenHeight = displayMetrics.heightPixels.coerceAtLeast(1280)

        // Calculate size from percentages
        val width = (screenWidth * (preset.widthPercent.coerceIn(20, 100) / 100f)).toInt()
        val height = (screenHeight * (preset.heightPercent.coerceIn(20, 100) / 100f)).toInt()

        // Calculate position
        val maxX = (screenWidth - width).coerceAtLeast(0)
        val maxY = (screenHeight - height).coerceAtLeast(0)

        val left = (maxX * (preset.positionXPercent.coerceIn(0, 100) / 100f)).toInt()
        val top = (maxY * (preset.positionYPercent.coerceIn(0, 100) / 100f)).toInt()
        val right = left + width
        val bottom = top + height

        return Rect(left, top, right, bottom)
    }

    fun launchAppInFreeform(
        context: Context,
        packageName: String,
        preset: WindowPreset,
        useMultipleTaskFlags: Boolean = true
    ): Boolean {
        return try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(packageName) ?: return false

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (useMultipleTaskFlags) {
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }

            val bounds = calculateLaunchBounds(context, preset)
            val options = ActivityOptions.makeBasic()
            options.setLaunchBounds(bounds)

            context.startActivity(intent, options.toBundle())
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Launch failed: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun openDeveloperSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (e2: Exception) {
                Toast.makeText(context, "Cannot open Settings", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getAdbCommandForDeveloperOptions(): String {
        return "adb shell settings put global enable_freeform_support 1 && adb shell settings put global force_resizable_activities 1"
    }

    fun getAdbLaunchCommand(packageName: String, bounds: Rect): String {
        return "adb shell am start -n $packageName --windowingMode 5 -b [${bounds.left},${bounds.top}][${bounds.right},${bounds.bottom}]"
    }
}
