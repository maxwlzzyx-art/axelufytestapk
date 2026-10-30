package com.example.ui.screens

import android.content.Context
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.model.FloatingToolType
import com.example.service.MiFreeformOverlayService
import com.example.ui.components.FloatingWindowSandbox
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MiCyan
import com.example.ui.theme.MiElectricBlue

@Composable
fun FloatingToolsScreen(
    preferenceManager: PreferenceManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeSandboxTool by remember { mutableStateOf<FloatingToolType?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.mini_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.mini_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Cards for the 4 Tools
            val tools = listOf(
                Triple(
                    FloatingToolType.CALCULATOR,
                    R.string.mini_calc_title,
                    R.string.mini_calc_desc
                ),
                Triple(
                    FloatingToolType.NOTES,
                    R.string.mini_notes_title,
                    R.string.mini_notes_desc
                ),
                Triple(
                    FloatingToolType.STOPWATCH,
                    R.string.mini_timer_title,
                    R.string.mini_timer_desc
                ),
                Triple(
                    FloatingToolType.BROWSER,
                    R.string.mini_browser_title,
                    R.string.mini_browser_desc
                )
            )

            tools.forEach { (toolType, titleRes, descRes) ->
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Brush.linearGradient(listOf(MiElectricBlue, MiCyan))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (toolType) {
                                        FloatingToolType.CALCULATOR -> "🧮"
                                        FloatingToolType.NOTES -> "📝"
                                        FloatingToolType.STOPWATCH -> "⏱️"
                                        FloatingToolType.BROWSER -> "🌐"
                                    },
                                    fontSize = 24.sp
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(titleRes),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(descRes),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { activeSandboxTool = toolType },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MiElectricBlue),
                                modifier = Modifier.weight(1f).testTag("btn_sandbox_${toolType.name}")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.mini_in_app_preview), fontSize = 11.sp)
                            }

                            FilledTonalButton(
                                onClick = {
                                    if (Settings.canDrawOverlays(context)) {
                                        preferenceManager.isDockEnabled = true
                                        MiFreeformOverlayService.start(context)
                                        Toast.makeText(context, "Floating Dock opened! Tap tool icon in dock.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Please grant overlay permission in Dashboard first", Toast.LENGTH_LONG).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("btn_overlay_${toolType.name}")
                            ) {
                                Text(stringResource(R.string.mini_launch), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Active In-App Sandbox Floating Window Overlay
        activeSandboxTool?.let { tool ->
            FloatingWindowSandbox(
                toolType = tool,
                onClose = { activeSandboxTool = null },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
