package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppRepository
import com.example.data.PreferenceManager
import com.example.model.InstalledApp
import com.example.model.WindowPreset
import com.example.ui.screens.AppRulesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FloatingToolsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WindowSizesScreen
import com.example.ui.theme.MiCyan
import com.example.ui.theme.MiElectricBlue
import com.example.ui.theme.MyApplicationTheme
import com.example.util.FreeformLauncherHelper
import com.example.util.LocaleHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var appRepository: AppRepository

    override fun attachBaseContext(newBase: Context) {
        val updatedContext = LocaleHelper.applySavedLocale(newBase)
        super.attachBaseContext(updatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferenceManager = PreferenceManager(this)
        appRepository = AppRepository(this)

        setContent {
            MyApplicationTheme {
                MainAppScreen(
                    preferenceManager = preferenceManager,
                    appRepository = appRepository,
                    onRestartActivity = {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        finish()
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

enum class NavigationTab(val titleRes: Int, val icon: ImageVector) {
    DASHBOARD(R.string.nav_dashboard, Icons.Default.Dashboard),
    WINDOW_SIZES(R.string.nav_window_sizes, Icons.Default.Tune),
    APP_RULES(R.string.nav_app_rules, Icons.Default.Layers),
    FLOATING_TOOLS(R.string.nav_mini_apps, Icons.Default.Widgets),
    SETTINGS(R.string.nav_settings, Icons.Default.Settings)
}

@Composable
fun MainAppScreen(
    preferenceManager: PreferenceManager,
    appRepository: AppRepository,
    onRestartActivity: () -> Unit
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    var presets by remember { mutableStateOf(preferenceManager.getPresets()) }
    var installedApps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        installedApps = appRepository.getInstalledLaunchableApps()
    }

    val activeDefaultPreset = remember(presets, preferenceManager.getDefaultPresetId()) {
        preferenceManager.getDefaultPreset()
    }

    BackHandler(enabled = currentTab != NavigationTab.DASHBOARD) {
        currentTab = NavigationTab.DASHBOARD
    }

    fun launchApp(app: InstalledApp, preset: WindowPreset) {
        val success = FreeformLauncherHelper.launchAppInFreeform(
            context = context,
            packageName = app.packageName,
            preset = preset,
            useMultipleTaskFlags = preferenceManager.isMultipleTaskFlagsEnabled
        )
        if (success) {
            Toast.makeText(context, "Launched ${app.appName} in ${preset.name}", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = stringResource(tab.titleRes)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(tab.titleRes),
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MiCyan,
                            selectedTextColor = MiElectricBlue,
                            indicatorColor = MiElectricBlue.copy(alpha = 0.18f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabTransition"
            ) { targetTab ->
                when (targetTab) {
                    NavigationTab.DASHBOARD -> {
                        DashboardScreen(
                            preferenceManager = preferenceManager,
                            installedApps = installedApps,
                            activePreset = activeDefaultPreset,
                            onNavigateToWindowSizes = { currentTab = NavigationTab.WINDOW_SIZES },
                            onNavigateToAppRules = { currentTab = NavigationTab.APP_RULES },
                            onLaunchApp = { app ->
                                val preset = preferenceManager.getPresetForApp(app.packageName)
                                launchApp(app, preset)
                            }
                        )
                    }

                    NavigationTab.WINDOW_SIZES -> {
                        WindowSizesScreen(
                            preferenceManager = preferenceManager,
                            presets = presets,
                            onPresetsUpdated = { updated -> presets = updated },
                            onTestLaunchPreset = { preset ->
                                val targetApp = installedApps.firstOrNull()
                                if (targetApp != null) {
                                    launchApp(targetApp, preset)
                                } else {
                                    Toast.makeText(context, "No launchable app found to test", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    NavigationTab.APP_RULES -> {
                        AppRulesScreen(
                            preferenceManager = preferenceManager,
                            installedApps = installedApps,
                            presets = presets,
                            onLaunchAppWithRule = { pkg, preset ->
                                val app = installedApps.firstOrNull { it.packageName == pkg }
                                    ?: InstalledApp(pkg, pkg)
                                launchApp(app, preset)
                            }
                        )
                    }

                    NavigationTab.FLOATING_TOOLS -> {
                        FloatingToolsScreen(
                            preferenceManager = preferenceManager
                        )
                    }

                    NavigationTab.SETTINGS -> {
                        SettingsScreen(
                            preferenceManager = preferenceManager,
                            onLanguageChanged = { _ ->
                                onRestartActivity()
                            }
                        )
                    }
                }
            }
        }
    }
}
