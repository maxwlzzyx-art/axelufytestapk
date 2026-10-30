package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.WindowPreset
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MiCyan
import com.example.ui.theme.MiElectricBlue
import kotlin.math.roundToInt

@Composable
fun InteractiveWindowCanvas(
    preset: WindowPreset,
    onPresetChanged: (WindowPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    var widthPct by remember(preset) { mutableFloatStateOf(preset.widthPercent.toFloat()) }
    var heightPct by remember(preset) { mutableFloatStateOf(preset.heightPercent.toFloat()) }
    var posX by remember(preset) { mutableFloatStateOf(preset.positionXPercent.toFloat()) }
    var posY by remember(preset) { mutableFloatStateOf(preset.positionYPercent.toFloat()) }
    var cornerRadius by remember(preset) { mutableFloatStateOf(preset.cornerRadiusDp.toFloat()) }
    var opacity by remember(preset) { mutableFloatStateOf(preset.opacityPercent.toFloat()) }

    fun commitChanges() {
        onPresetChanged(
            preset.copy(
                widthPercent = widthPct.roundToInt().coerceIn(20, 100),
                heightPercent = heightPct.roundToInt().coerceIn(20, 100),
                positionXPercent = posX.roundToInt().coerceIn(0, 100),
                positionYPercent = posY.roundToInt().coerceIn(0, 100),
                cornerRadiusDp = cornerRadius.roundToInt().coerceIn(4, 32),
                opacityPercent = opacity.roundToInt().coerceIn(40, 100)
            )
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Device Screen Mockup Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.ws_interactive_preview),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.ws_preview_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Phone Display Simulation
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(9f / 16f)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E293B),
                                    Color(0xFF0F172A)
                                )
                            )
                        )
                        .border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(28.dp)
                        )
                ) {
                    val containerWidthPx = constraints.maxWidth.toFloat()
                    val containerHeightPx = constraints.maxHeight.toFloat()

                    // Top camera notch & status bar mockup
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                            .size(width = 60.dp, height = 14.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                    )

                    // Bottom gesture pill mockup
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                            .size(width = 80.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.4f))
                    )

                    // Calculated window box dimensions in pixels
                    val winWidthPx = containerWidthPx * (widthPct / 100f)
                    val winHeightPx = containerHeightPx * (heightPct / 100f)

                    val maxOffsetX = (containerWidthPx - winWidthPx).coerceAtLeast(0f)
                    val maxOffsetY = (containerHeightPx - winHeightPx).coerceAtLeast(0f)

                    val currentOffsetX = maxOffsetX * (posX / 100f)
                    val currentOffsetY = maxOffsetY * (posY / 100f)

                    // Floating Window inside canvas
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    currentOffsetX.roundToInt(),
                                    currentOffsetY.roundToInt()
                                )
                            }
                            .size(
                                width = with(LocalDensity.current) { winWidthPx.toDp() },
                                height = with(LocalDensity.current) { winHeightPx.toDp() }
                            )
                            .clip(RoundedCornerShape((cornerRadius * (winWidthPx / containerWidthPx)).coerceAtLeast(6f).dp))
                            .background(
                                DarkSurface.copy(alpha = (opacity / 100f).coerceIn(0.4f, 1f))
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    listOf(MiCyan, MiElectricBlue)
                                ),
                                shape = RoundedCornerShape((cornerRadius * (winWidthPx / containerWidthPx)).coerceAtLeast(6f).dp)
                            )
                            .pointerInput(containerWidthPx, containerHeightPx) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    if (maxOffsetX > 0) {
                                        val newOffsetX = (currentOffsetX + dragAmount.x).coerceIn(0f, maxOffsetX)
                                        posX = (newOffsetX / maxOffsetX) * 100f
                                    }
                                    if (maxOffsetY > 0) {
                                        val newOffsetY = (currentOffsetY + dragAmount.y).coerceIn(0f, maxOffsetY)
                                        posY = (newOffsetY / maxOffsetY) * 100f
                                    }
                                    commitChanges()
                                }
                            }
                    ) {
                        // Title bar of floating preview
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.35f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = preset.name,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEAB308))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                )
                            }
                        }

                        // Window Info in Center
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${widthPct.roundToInt()}% × ${heightPct.roundToInt()}%",
                                color = MiCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val ratio = (widthPct / heightPct)
                            val ratioLabel = when {
                                ratio in 1.7f..1.85f -> "16:9"
                                ratio in 1.3f..1.4f -> "4:3"
                                ratio in 0.95f..1.05f -> "1:1"
                                ratio in 0.72f..0.78f -> "3:4"
                                ratio in 0.52f..0.6f -> "9:16"
                                else -> String.format("%.2f:1", ratio)
                            }
                            Text(
                                text = ratioLabel,
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 10.sp
                            )
                        }

                        // Resize handle in bottom-right corner
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(24.dp)
                                .pointerInput(containerWidthPx, containerHeightPx) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val addedWidthPct = (dragAmount.x / containerWidthPx) * 100f
                                        val addedHeightPct = (dragAmount.y / containerHeightPx) * 100f
                                        widthPct = (widthPct + addedWidthPct).coerceIn(20f, 100f)
                                        heightPct = (heightPct + addedHeightPct).coerceIn(20f, 100f)
                                        commitChanges()
                                    }
                                }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInFull,
                                contentDescription = stringResource(R.string.win_resize),
                                tint = MiCyan,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Shape Presets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = widthPct == 45f && heightPct == 40f,
                onClick = {
                    widthPct = 45f
                    heightPct = 40f
                    commitChanges()
                },
                label = { Text("Mini", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.CropPortrait, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            FilterChip(
                selected = widthPct == 80f && heightPct == 45f,
                onClick = {
                    widthPct = 80f
                    heightPct = 45f
                    commitChanges()
                },
                label = { Text("16:9", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.CropLandscape, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            FilterChip(
                selected = widthPct == 50f && heightPct == 96f,
                onClick = {
                    widthPct = 50f
                    heightPct = 96f
                    posX = 0f
                    commitChanges()
                },
                label = { Text("Split L", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Splitscreen, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            FilterChip(
                selected = widthPct == 70f && heightPct == 70f,
                onClick = {
                    widthPct = 70f
                    heightPct = 70f
                    commitChanges()
                },
                label = { Text("1:1", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.CropSquare, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sliders for Precise Adjustment
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // Width Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.ws_width),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${widthPct.roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = widthPct,
                onValueChange = {
                    widthPct = it
                    commitChanges()
                },
                valueRange = 20f..100f,
                modifier = Modifier.testTag("slider_width")
            )

            // Height Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.ws_height),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${heightPct.roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = heightPct,
                onValueChange = {
                    heightPct = it
                    commitChanges()
                },
                valueRange = 20f..100f,
                modifier = Modifier.testTag("slider_height")
            )

            // Position X Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.ws_position_x),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${posX.roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = posX,
                onValueChange = {
                    posX = it
                    commitChanges()
                },
                valueRange = 0f..100f,
                modifier = Modifier.testTag("slider_pos_x")
            )

            // Position Y Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.ws_position_y),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${posY.roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = posY,
                onValueChange = {
                    posY = it
                    commitChanges()
                },
                valueRange = 0f..100f,
                modifier = Modifier.testTag("slider_pos_y")
            )

            // Corner Radius Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.ws_corner_radius),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${cornerRadius.roundToInt()} dp",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = cornerRadius,
                onValueChange = {
                    cornerRadius = it
                    commitChanges()
                },
                valueRange = 4f..32f,
                modifier = Modifier.testTag("slider_corner_radius")
            )

            // Opacity Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.ws_opacity),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${opacity.roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = opacity,
                onValueChange = {
                    opacity = it
                    commitChanges()
                },
                valueRange = 40f..100f,
                modifier = Modifier.testTag("slider_opacity")
            )
        }
    }
}
