package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.ViewSidebar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PreferenceManager
import com.example.ui.theme.MiCyan
import com.example.ui.theme.MiElectricBlue
import com.example.util.FreeformLauncherHelper
import com.example.util.LocaleHelper

@Composable
fun SettingsScreen(
    preferenceManager: PreferenceManager,
    onLanguageChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isRightSide by remember { mutableStateOf(preferenceManager.isDockOnRightSide) }
    var isAutoHide by remember { mutableStateOf(preferenceManager.isDockAutoHide) }
    var isHaptic by remember { mutableStateOf(preferenceManager.isHapticFeedbackEnabled) }
    var isMultiTaskFlags by remember { mutableStateOf(preferenceManager.isMultipleTaskFlagsEnabled) }
    var currentLang by remember { mutableStateOf(LocaleHelper.getSavedLanguage(context)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.set_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Language Selection Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = MiCyan)
                    Text(
                        text = stringResource(R.string.set_language_section),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = currentLang == LocaleHelper.LANG_SYSTEM,
                        onClick = {
                            currentLang = LocaleHelper.LANG_SYSTEM
                            LocaleHelper.setLanguage(context, LocaleHelper.LANG_SYSTEM)
                            onLanguageChanged(LocaleHelper.LANG_SYSTEM)
                        },
                        label = { Text(stringResource(R.string.set_lang_system), fontSize = 11.sp) },
                        leadingIcon = if (currentLang == LocaleHelper.LANG_SYSTEM) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        modifier = Modifier.testTag("chip_lang_system")
                    )

                    FilterChip(
                        selected = currentLang == LocaleHelper.LANG_EN,
                        onClick = {
                            currentLang = LocaleHelper.LANG_EN
                            LocaleHelper.setLanguage(context, LocaleHelper.LANG_EN)
                            onLanguageChanged(LocaleHelper.LANG_EN)
                        },
                        label = { Text(stringResource(R.string.set_lang_en), fontSize = 11.sp) },
                        leadingIcon = if (currentLang == LocaleHelper.LANG_EN) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        modifier = Modifier.testTag("chip_lang_en")
                    )

                    FilterChip(
                        selected = currentLang == LocaleHelper.LANG_ID,
                        onClick = {
                            currentLang = LocaleHelper.LANG_ID
                            LocaleHelper.setLanguage(context, LocaleHelper.LANG_ID)
                            onLanguageChanged(LocaleHelper.LANG_ID)
                        },
                        label = { Text(stringResource(R.string.set_lang_in), fontSize = 11.sp) },
                        leadingIcon = if (currentLang == LocaleHelper.LANG_ID) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        modifier = Modifier.testTag("chip_lang_in")
                    )
                }
            }
        }

        // Floating Dock Appearance Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.ViewSidebar, contentDescription = null, tint = MiElectricBlue)
                    Text(
                        text = stringResource(R.string.set_dock_section),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Handle Side (Left vs Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.set_dock_side), style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !isRightSide,
                            onClick = {
                                isRightSide = false
                                preferenceManager.isDockOnRightSide = false
                            },
                            label = { Text(stringResource(R.string.set_dock_side_left), fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = isRightSide,
                            onClick = {
                                isRightSide = true
                                preferenceManager.isDockOnRightSide = true
                            },
                            label = { Text(stringResource(R.string.set_dock_side_right), fontSize = 11.sp) }
                        )
                    }
                }

                // Auto-Hide Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.set_auto_hide), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Switch(
                        checked = isAutoHide,
                        onCheckedChange = {
                            isAutoHide = it
                            preferenceManager.isDockAutoHide = it
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = MiElectricBlue)
                    )
                }

                // Haptic Feedback Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.set_vibration), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Switch(
                        checked = isHaptic,
                        onCheckedChange = {
                            isHaptic = it
                            preferenceManager.isHapticFeedbackEnabled = it
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = MiElectricBlue)
                    )
                }
            }
        }

        // Android 15 & Multitasking Flags Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.DeveloperMode, contentDescription = null, tint = MiCyan)
                    Text(
                        text = stringResource(R.string.set_android15_section),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.set_multi_task_flags),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.set_multi_task_flags_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isMultiTaskFlags,
                        onCheckedChange = {
                            isMultiTaskFlags = it
                            preferenceManager.isMultipleTaskFlagsEnabled = it
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = MiElectricBlue)
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Shizuku / Shell Integration Info
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.set_shizuku_mode),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.set_shizuku_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("ADB Command", FreeformLauncherHelper.getAdbCommandForDeveloperOptions())
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, context.getString(R.string.dash_adb_copied), Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_copy_shizuku_adb")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.dash_copy_adb_command), fontSize = 12.sp)
                    }
                }
            }
        }

        // About & Project Credits
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "About Mi Freeform Remake",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Remade from sunshine0523/Mi-Freeform with modern Jetpack Compose UI, interactive window sizing canvas, Indonesian language support, and Android 15 windowing API optimizations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sunshine0523/Mi-Freeform"))
                            context.startActivity(intent)
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = MiCyan, modifier = Modifier.size(16.dp))
                    Text(
                        text = "github.com/sunshine0523/Mi-Freeform",
                        color = MiCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
