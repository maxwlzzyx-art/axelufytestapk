package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.model.FloatingToolType
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MiAccentGreen
import com.example.ui.theme.MiAccentOrange
import com.example.ui.theme.MiCyan
import com.example.ui.theme.MiElectricBlue
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun FloatingWindowSandbox(
    toolType: FloatingToolType,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMinimized by remember { mutableStateOf(false) }
    var posX by remember { mutableFloatStateOf(40f) }
    var posY by remember { mutableFloatStateOf(80f) }
    var winWidth by remember { mutableFloatStateOf(340f) }
    var winHeight by remember { mutableFloatStateOf(460f) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val maxAvailableWidth = constraints.maxWidth.toFloat()
        val maxAvailableHeight = constraints.maxHeight.toFloat()

        if (isMinimized) {
            // Floating bubble pill docked at edge
            Box(
                modifier = Modifier
                    .offset { IntOffset(posX.roundToInt().coerceIn(0, (maxAvailableWidth - 140).toInt()), posY.roundToInt().coerceIn(0, (maxAvailableHeight - 140).toInt())) }
                    .size(64.dp)
                    .clip(CircleShape)
                    .shadow(12.dp, CircleShape)
                    .background(
                        Brush.linearGradient(listOf(MiCyan, MiElectricBlue))
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    .clickable { isMinimized = false }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            posX += dragAmount.x
                            posY += dragAmount.y
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (toolType) {
                        FloatingToolType.CALCULATOR -> "🧮"
                        FloatingToolType.NOTES -> "📝"
                        FloatingToolType.STOPWATCH -> "⏱️"
                        FloatingToolType.BROWSER -> "🌐"
                    },
                    fontSize = 28.sp
                )
            }
        } else {
            // Full floating window
            Surface(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            posX.roundToInt().coerceIn(0, (maxAvailableWidth - winWidth).coerceAtLeast(0f).toInt()),
                            posY.roundToInt().coerceIn(0, (maxAvailableHeight - winHeight).coerceAtLeast(0f).toInt())
                        )
                    }
                    .size(winWidth.dp, winHeight.dp)
                    .shadow(16.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        1.5.dp,
                        Brush.linearGradient(listOf(MiCyan, MiElectricBlue)),
                        RoundedCornerShape(20.dp)
                    ),
                color = DarkSurface,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Draggable Titlebar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(DarkSurfaceElevated, DarkBackground)
                                )
                            )
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    posX += dragAmount.x
                                    posY += dragAmount.y
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = when (toolType) {
                                    FloatingToolType.CALCULATOR -> "🧮"
                                    FloatingToolType.NOTES -> "📝"
                                    FloatingToolType.STOPWATCH -> "⏱️"
                                    FloatingToolType.BROWSER -> "🌐"
                                },
                                fontSize = 16.sp
                            )
                            Text(
                                text = stringResource(toolType.titleResId),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Window Control Buttons (Minimize, Close)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { isMinimized = true },
                                modifier = Modifier.size(28.dp).testTag("btn_win_minimize")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Minimize,
                                    contentDescription = stringResource(R.string.win_minimize),
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier.size(28.dp).testTag("btn_win_close")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.win_close),
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Window Body Content
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when (toolType) {
                            FloatingToolType.CALCULATOR -> FloatingCalculatorContent()
                            FloatingToolType.NOTES -> FloatingNotesContent()
                            FloatingToolType.STOPWATCH -> FloatingStopwatchContent()
                            FloatingToolType.BROWSER -> FloatingBrowserContent()
                        }

                        // Resize Grip in bottom-right corner
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(28.dp)
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        winWidth = (winWidth + dragAmount.x).coerceIn(240f, 600f)
                                        winHeight = (winHeight + dragAmount.y).coerceIn(300f, 800f)
                                    }
                                }
                                .padding(4.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInFull,
                                contentDescription = stringResource(R.string.win_resize),
                                tint = MiCyan.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------- Calculator -----------------
@Composable
private fun FloatingCalculatorContent() {
    var display by remember { mutableStateOf("0") }
    var expression by remember { mutableStateOf("") }

    fun onDigit(d: String) {
        if (display == "0" || display == "Error") {
            display = d
        } else {
            display += d
        }
    }

    fun onOp(op: String) {
        expression = "$display $op "
        display = "0"
    }

    fun onEqual() {
        if (expression.isNotEmpty()) {
            val parts = expression.trim().split(" ")
            if (parts.size >= 2) {
                val num1 = parts[0].toDoubleOrNull() ?: 0.0
                val op = parts[1]
                val num2 = display.toDoubleOrNull() ?: 0.0
                val result = when (op) {
                    "+" -> num1 + num2
                    "-" -> num1 - num2
                    "×", "*" -> num1 * num2
                    "÷", "/" -> if (num2 != 0.0) num1 / num2 else Double.NaN
                    else -> num2
                }
                display = if (result.isNaN()) {
                    "Error"
                } else if (result % 1.0 == 0.0) {
                    result.toLong().toString()
                } else {
                    String.format("%.4f", result).trimEnd('0').trimEnd('.')
                }
                expression = ""
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkBackground)
                .padding(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = expression,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
            Text(
                text = display,
                color = MiCyan,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Buttons Grid
        val rows = listOf(
            listOf("C", "⌫", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=")
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { btn ->
                        val isOp = btn in listOf("÷", "×", "-", "+", "=")
                        val isSpecial = btn in listOf("C", "⌫", "%")
                        val weight = if (btn == "0") 2f else 1f

                        Box(
                            modifier = Modifier
                                .weight(weight)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        btn == "=" -> MiElectricBlue
                                        isOp -> Color(0xFF1E3A8A)
                                        isSpecial -> Color(0xFF374151)
                                        else -> DarkSurfaceElevated
                                    }
                                )
                                .clickable {
                                    when (btn) {
                                        "C" -> {
                                            display = "0"
                                            expression = ""
                                        }
                                        "⌫" -> {
                                            display = if (display.length > 1) display.dropLast(1) else "0"
                                        }
                                        "%" -> {
                                            val v = display.toDoubleOrNull() ?: 0.0
                                            display = (v / 100.0).toString()
                                        }
                                        "÷", "×", "-", "+" -> onOp(btn)
                                        "=" -> onEqual()
                                        "." -> if (!display.contains(".")) display += "."
                                        else -> onDigit(btn)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = btn,
                                color = if (btn == "=" || isOp) Color.White else Color(0xFFF3F4F6),
                                fontSize = 16.sp,
                                fontWeight = if (isOp || btn == "=") FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------- Notes -----------------
@Composable
private fun FloatingNotesContent() {
    val context = LocalContext.current
    var noteText by remember { mutableStateOf("Quick scratchpad:\n• Launch games in freeform\n• Check Discord while playing\n• Resizable multitasking window!") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.notes_save),
                fontSize = 11.sp,
                color = MiAccentGreen
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Floating Note", noteText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, context.getString(R.string.action_copied), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = MiCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = { noteText = "" },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            placeholder = { Text(stringResource(R.string.notes_placeholder), fontSize = 13.sp) },
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkBackground,
                unfocusedContainerColor = DarkBackground,
                focusedBorderColor = MiCyan,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color(0xFFE5E7EB)
            )
        )
    }
}

// ----------------- Stopwatch -----------------
@Composable
private fun FloatingStopwatchContent() {
    var isRunning by remember { mutableStateOf(false) }
    var elapsedMillis by remember { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<String>() }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(100)
            elapsedMillis += 100
        }
    }

    fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        val tenths = (ms % 1000) / 100
        return String.format("%02d:%02d.%01d", min, sec, tenths)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatTime(elapsedMillis),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MiCyan,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { isRunning = !isRunning },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) MiAccentOrange else MiAccentGreen
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(if (isRunning) stringResource(R.string.timer_pause) else stringResource(R.string.timer_start))
            }

            if (isRunning) {
                FilledTonalButton(
                    onClick = { laps.add(0, formatTime(elapsedMillis)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(stringResource(R.string.timer_lap))
                }
            } else if (elapsedMillis > 0) {
                FilledTonalButton(
                    onClick = {
                        elapsedMillis = 0L
                        laps.clear()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(stringResource(R.string.timer_reset))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkBackground)
                .padding(6.dp)
        ) {
            items(laps) { lap ->
                Text(
                    text = "Lap: $lap",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

// ----------------- Browser -----------------
@Composable
private fun FloatingBrowserContent() {
    var urlInput by remember { mutableStateOf("https://wikipedia.org") }
    var currentUrl by remember { mutableStateOf("https://wikipedia.org") }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Address bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBackground)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (webViewRef?.canGoBack() == true) webViewRef?.goBack() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(16.dp))
            }
            IconButton(
                onClick = { webViewRef?.reload() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = Color.White, modifier = Modifier.size(16.dp))
            }

            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(20.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(
                    onGo = {
                        val finalUrl = if (!urlInput.startsWith("http://") && !urlInput.startsWith("https://")) {
                            "https://$urlInput"
                        } else urlInput
                        currentUrl = finalUrl
                        webViewRef?.loadUrl(finalUrl)
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated,
                    focusedBorderColor = MiCyan,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            IconButton(
                onClick = {
                    val finalUrl = if (!urlInput.startsWith("http://") && !urlInput.startsWith("https://")) {
                        "https://$urlInput"
                    } else urlInput
                    currentUrl = finalUrl
                    webViewRef?.loadUrl(finalUrl)
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Go", tint = MiCyan, modifier = Modifier.size(18.dp))
            }
        }

        // Live WebView
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            url?.let {
                                urlInput = it
                                currentUrl = it
                            }
                        }
                    }
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    loadUrl(currentUrl)
                    webViewRef = this
                }
            },
            update = { view ->
                webViewRef = view
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}
