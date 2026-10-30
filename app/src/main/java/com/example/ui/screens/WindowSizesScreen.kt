package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PreferenceManager
import com.example.model.WindowPreset
import com.example.ui.components.InteractiveWindowCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MiAccentGreen
import com.example.ui.theme.MiCyan
import com.example.ui.theme.MiElectricBlue
import java.util.UUID
import kotlin.math.roundToInt

@Composable
fun WindowSizesScreen(
    preferenceManager: PreferenceManager,
    presets: List<WindowPreset>,
    onPresetsUpdated: (List<WindowPreset>) -> Unit,
    onTestLaunchPreset: (WindowPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val defaultPresetId = preferenceManager.getDefaultPresetId()
    var selectedPreset by remember(presets, defaultPresetId) {
        mutableStateOf(presets.firstOrNull { it.id == defaultPresetId } ?: presets.first())
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var presetToDelete by remember { mutableStateOf<WindowPreset?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Title & Description
        Column {
            Text(
                text = stringResource(R.string.ws_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.ws_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Interactive Canvas Preview & Sliders
        InteractiveWindowCanvas(
            preset = selectedPreset,
            onPresetChanged = { updated ->
                selectedPreset = updated
                preferenceManager.savePreset(updated)
                onPresetsUpdated(preferenceManager.getPresets())
            }
        )

        // Actions Row for Selected Preset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isCurrentDefault = selectedPreset.id == preferenceManager.getDefaultPresetId()

            Button(
                onClick = {
                    preferenceManager.setDefaultPresetId(selectedPreset.id)
                    onPresetsUpdated(preferenceManager.getPresets())
                    Toast.makeText(context, "${selectedPreset.name} set as default", Toast.LENGTH_SHORT).show()
                },
                enabled = !isCurrentDefault,
                colors = ButtonDefaults.buttonColors(containerColor = MiElectricBlue),
                modifier = Modifier.weight(1f).testTag("btn_set_default_preset")
            ) {
                Icon(
                    imageVector = if (isCurrentDefault) Icons.Default.Check else Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isCurrentDefault) stringResource(R.string.ws_is_default) else stringResource(R.string.ws_set_default),
                    fontSize = 12.sp
                )
            }

            FilledTonalButton(
                onClick = { onTestLaunchPreset(selectedPreset) },
                modifier = Modifier.weight(1f).testTag("btn_test_launch_preset")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.action_launch), fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Presets Header & Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.ws_presets_header),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_add_preset")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.ws_add_preset), fontSize = 12.sp)
            }
        }

        // List of Available Presets
        presets.forEach { preset ->
            val isSelected = preset.id == selectedPreset.id
            val isDefault = preset.id == defaultPresetId

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedPreset = preset }
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MiCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (isDefault) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MiAccentGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.ws_is_default),
                                        color = MiAccentGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${preset.widthPercent}% Width × ${preset.heightPercent}% Height • Radius: ${preset.cornerRadiusDp}dp",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (!preset.isBuiltIn) {
                            IconButton(
                                onClick = { presetToDelete = preset },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Create New Preset Dialog
    if (showCreateDialog) {
        var newName by remember { mutableStateOf("") }
        var newWidth by remember { mutableFloatStateOf(60f) }
        var newHeight by remember { mutableFloatStateOf(50f) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(stringResource(R.string.ws_add_preset), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(stringResource(R.string.ws_preset_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Width: ${newWidth.roundToInt()}%", fontSize = 12.sp)
                    Slider(
                        value = newWidth,
                        onValueChange = { newWidth = it },
                        valueRange = 25f..100f
                    )

                    Text("Height: ${newHeight.roundToInt()}%", fontSize = 12.sp)
                    Slider(
                        value = newHeight,
                        onValueChange = { newHeight = it },
                        valueRange = 25f..100f
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newName.trim().ifEmpty { "Preset ${presets.size + 1}" }
                        val newPreset = WindowPreset(
                            id = "custom_" + UUID.randomUUID().toString().take(6),
                            name = trimmed,
                            widthPercent = newWidth.roundToInt(),
                            heightPercent = newHeight.roundToInt(),
                            positionXPercent = 50,
                            positionYPercent = 50,
                            cornerRadiusDp = 16,
                            isBuiltIn = false
                        )
                        preferenceManager.savePreset(newPreset)
                        selectedPreset = newPreset
                        onPresetsUpdated(preferenceManager.getPresets())
                        showCreateDialog = false
                    }
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    // Delete Preset Confirmation Dialog
    presetToDelete?.let { preset ->
        AlertDialog(
            onDismissRequest = { presetToDelete = null },
            title = { Text(stringResource(R.string.ws_delete_preset)) },
            text = { Text(stringResource(R.string.confirm_delete_preset)) },
            confirmButton = {
                Button(
                    onClick = {
                        preferenceManager.deletePreset(preset.id)
                        onPresetsUpdated(preferenceManager.getPresets())
                        presetToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { presetToDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
