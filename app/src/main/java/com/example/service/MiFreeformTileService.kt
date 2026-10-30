package com.example.service

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.example.MainActivity
import com.example.R
import com.example.data.PreferenceManager

@RequiresApi(Build.VERSION_CODES.N)
class MiFreeformTileService : TileService() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate() {
        super.onCreate()
        preferenceManager = PreferenceManager(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()

        if (!Settings.canDrawOverlays(this)) {
            // Overlay permission is required first
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+ pending intent for tile
                startActivityAndCollapse(intent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
            return
        }

        val currentState = preferenceManager.isDockEnabled
        val newState = !currentState
        preferenceManager.isDockEnabled = newState

        if (newState) {
            MiFreeformOverlayService.start(this)
        } else {
            MiFreeformOverlayService.stop(this)
        }

        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isActive = preferenceManager.isDockEnabled && Settings.canDrawOverlays(this)

        tile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.tile_label)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (isActive) getString(R.string.tile_state_active) else getString(R.string.tile_state_inactive)
        }
        tile.updateTile()
    }
}
