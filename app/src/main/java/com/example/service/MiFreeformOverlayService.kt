package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.MiFreeformApp
import com.example.R
import com.example.data.AppRepository
import com.example.data.PreferenceManager
import com.example.model.FloatingToolType
import com.example.model.InstalledApp
import com.example.ui.components.FloatingWindowSandbox
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MiCyan
import com.example.ui.theme.MiElectricBlue
import com.example.ui.theme.MyApplicationTheme
import com.example.util.FreeformLauncherHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MiFreeformOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayContainer: FrameLayout? = null
    private var composeView: ComposeView? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var appRepository: AppRepository

    private var isExpanded = mutableStateOf(false)
    private var activeTool = mutableStateOf<FloatingToolType?>(null)
    private var installedApps = mutableStateOf<List<InstalledApp>>(emptyList())

    private val overlayLifecycleOwner = OverlayLifecycleOwner()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        overlayLifecycleOwner.onCreate()
        preferenceManager = PreferenceManager(this)
        appRepository = AppRepository(this)

        startForeground(
            MiFreeformApp.NOTIFICATION_ID_OVERLAY,
            createNotification()
        )

        loadApps()
        showOverlayView()
    }

    private fun loadApps() {
        serviceScope.launch {
            val apps = appRepository.getInstalledLaunchableApps()
            installedApps.value = apps
        }
    }

    private fun createNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, MiFreeformApp.CHANNEL_ID_OVERLAY)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.dash_status_service_running))
            .setSmallIcon(R.drawable.ic_tile_freeform)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun showOverlayView() {
        if (!Settings.canDrawOverlays(this)) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val container = FrameLayout(this).apply {
            setViewTreeLifecycleOwner(overlayLifecycleOwner)
            setViewTreeViewModelStoreOwner(overlayLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(overlayLifecycleOwner)
        }
        overlayContainer = container

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = if (preferenceManager.isDockOnRightSide) Gravity.END or Gravity.CENTER_VERTICAL
                      else Gravity.START or Gravity.CENTER_VERTICAL
            x = 0
            y = 0
        }

        composeView = ComposeView(this).apply {
            setContent {
                MyApplicationTheme(darkTheme = true) {
                    DockOverlayContent(
                        isExpanded = isExpanded.value,
                        onToggleExpand = {
                            isExpanded.value = !isExpanded.value
                            updateWindowLayoutParams(isExpanded.value || activeTool.value != null)
                        },
                        activeTool = activeTool.value,
                        onSelectTool = { tool ->
                            activeTool.value = tool
                            isExpanded.value = false
                            updateWindowLayoutParams(true)
                        },
                        onCloseTool = {
                            activeTool.value = null
                            updateWindowLayoutParams(isExpanded.value)
                        },
                        apps = installedApps.value,
                        onLaunchApp = { app ->
                            val preset = preferenceManager.getPresetForApp(app.packageName)
                            FreeformLauncherHelper.launchAppInFreeform(
                                this@MiFreeformOverlayService,
                                app.packageName,
                                preset,
                                preferenceManager.isMultipleTaskFlagsEnabled
                            )
                            isExpanded.value = false
                            updateWindowLayoutParams(activeTool.value != null)
                        }
                    )
                }
            }
        }

        overlayContainer?.addView(composeView)
        try {
            windowManager?.addView(overlayContainer, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateWindowLayoutParams(needsFullScreen: Boolean) {
        val container = overlayContainer ?: return
        val lp = container.layoutParams as? WindowManager.LayoutParams ?: return

        if (needsFullScreen) {
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT
            lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            lp.gravity = Gravity.FILL
        } else {
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            lp.gravity = if (preferenceManager.isDockOnRightSide) Gravity.END or Gravity.CENTER_VERTICAL
                         else Gravity.START or Gravity.CENTER_VERTICAL
        }
        windowManager?.updateViewLayout(container, lp)
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayLifecycleOwner.onDestroy()
        serviceScope.cancel()
        overlayContainer?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        fun start(context: Context) {
            if (Settings.canDrawOverlays(context)) {
                val intent = Intent(context, MiFreeformOverlayService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, MiFreeformOverlayService::class.java)
            context.stopService(intent)
        }
    }
}

@Composable
private fun DockOverlayContent(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    activeTool: FloatingToolType?,
    onSelectTool: (FloatingToolType) -> Unit,
    onCloseTool: () -> Unit,
    apps: List<InstalledApp>,
    onLaunchApp: (InstalledApp) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (activeTool != null) {
            // Render active floating mini-app
            FloatingWindowSandbox(
                toolType = activeTool,
                onClose = onCloseTool,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (isExpanded) {
            // Background scrim when drawer is open
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { onToggleExpand() }
            )

            // Expanded MIUI-style Floating Dock Drawer
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(310.dp)
                    .align(Alignment.CenterEnd),
                color = DarkSurface,
                shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                tonalElevation = 16.dp
            ) {
                var searchQuery by remember { mutableStateOf("") }
                val filteredApps = remember(searchQuery, apps) {
                    if (searchQuery.isBlank()) apps
                    else apps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.dash_toggle_dock),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = onToggleExpand) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Floating Tools Row
                    Text(
                        text = stringResource(R.string.mini_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MiCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FloatingToolType.values().forEach { tool ->
                            val iconText = when (tool) {
                                FloatingToolType.CALCULATOR -> "🧮"
                                FloatingToolType.NOTES -> "📝"
                                FloatingToolType.STOPWATCH -> "⏱️"
                                FloatingToolType.BROWSER -> "🌐"
                            }
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(DarkSurfaceElevated)
                                    .clickable { onSelectTool(tool) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(iconText, fontSize = 24.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(stringResource(R.string.ar_search_placeholder), fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MiCyan) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated,
                            focusedBorderColor = MiCyan,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Installed Apps List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredApps) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurfaceElevated)
                                    .clickable { onLaunchApp(app) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Brush.linearGradient(listOf(MiElectricBlue, MiCyan))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = app.appName.firstOrNull()?.uppercase() ?: "A",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }

                                Text(
                                    text = app.appName,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Sleek Floating Edge Dock Pill Handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 2.dp)
                    .width(14.dp)
                    .height(72.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                    .background(
                        Brush.verticalGradient(listOf(MiCyan, MiElectricBlue))
                    )
                    .clickable { onToggleExpand() }
            )
        }
    }
}
