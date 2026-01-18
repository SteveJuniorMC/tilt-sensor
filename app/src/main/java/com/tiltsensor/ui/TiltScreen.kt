@file:OptIn(ExperimentalMaterial3Api::class)

package com.tiltsensor.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiltsensor.MeasurementAxis
import com.tiltsensor.data.WheelieSession
import com.tiltsensor.ui.theme.*
import kotlin.math.abs

data class TiltState(
    val angle: Float = 0f,
    val isTared: Boolean = false,
    val isRunning: Boolean = false,
    val selectedAxis: MeasurementAxis = MeasurementAxis.PITCH,
    val sessionMaxAngle: Float = 0f,
    val currentWheelieMaxAngle: Float = 0f,
    val wheelieCount: Int = 0,
    val currentWheelieDurationMs: Long = 0,
    val sessionTotalDurationMs: Long = 0,
    val isInWheelie: Boolean = false,
    val history: List<WheelieSession> = emptyList(),
    val showHistory: Boolean = false
)

@Composable
fun TiltScreen(
    state: TiltState,
    onStartStop: () -> Unit,
    onTare: () -> Unit,
    onResetTare: () -> Unit,
    onAxisChange: (MeasurementAxis) -> Unit,
    onResetSession: () -> Unit,
    onToggleHistory: () -> Unit,
    onClearHistory: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        if (state.showHistory) {
            HistoryScreen(
                sessions = state.history,
                onBack = onToggleHistory,
                onClear = onClearHistory
            )
        } else {
            if (isLandscape) {
                LandscapeLayout(state, onStartStop, onTare, onResetTare, onAxisChange, onResetSession, onToggleHistory)
            } else {
                PortraitLayout(state, onStartStop, onTare, onResetTare, onAxisChange, onResetSession, onToggleHistory)
            }
        }
    }
}

@Composable
private fun PortraitLayout(
    state: TiltState,
    onStartStop: () -> Unit,
    onTare: () -> Unit,
    onResetTare: () -> Unit,
    onAxisChange: (MeasurementAxis) -> Unit,
    onResetSession: () -> Unit,
    onToggleHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Axis selector at top
        AxisSelector(
            selectedAxis = state.selectedAxis,
            onAxisChange = onAxisChange,
            enabled = !state.isRunning
        )

        Spacer(modifier = Modifier.weight(0.05f))

        AngleDisplay(
            angle = state.angle,
            isRunning = state.isRunning,
            modifier = Modifier.weight(0.35f)
        )

        if (state.isRunning) {
            StatsPanel(
                state = state,
                modifier = Modifier.weight(0.25f)
            )
        } else {
            Spacer(modifier = Modifier.weight(0.25f))
        }

        ButtonPanel(
            isRunning = state.isRunning,
            isTared = state.isTared,
            onStartStop = onStartStop,
            onTare = onTare,
            onResetTare = onResetTare,
            onResetSession = onResetSession,
            onToggleHistory = onToggleHistory,
            modifier = Modifier.weight(0.35f)
        )
    }
}

@Composable
private fun LandscapeLayout(
    state: TiltState,
    onStartStop: () -> Unit,
    onTare: () -> Unit,
    onResetTare: () -> Unit,
    onAxisChange: (MeasurementAxis) -> Unit,
    onResetSession: () -> Unit,
    onToggleHistory: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(0.5f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AxisSelector(
                selectedAxis = state.selectedAxis,
                onAxisChange = onAxisChange,
                enabled = !state.isRunning
            )
            Spacer(modifier = Modifier.height(16.dp))
            AngleDisplay(
                angle = state.angle,
                isRunning = state.isRunning,
                modifier = Modifier.weight(1f)
            )
        }

        Column(
            modifier = Modifier
                .weight(0.5f)
                .fillMaxHeight()
                .padding(start = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            if (state.isRunning) {
                StatsPanel(
                    state = state,
                    modifier = Modifier.weight(0.5f)
                )
            }

            ButtonPanel(
                isRunning = state.isRunning,
                isTared = state.isTared,
                onStartStop = onStartStop,
                onTare = onTare,
                onResetTare = onResetTare,
                onResetSession = onResetSession,
                onToggleHistory = onToggleHistory,
                modifier = Modifier.weight(0.5f)
            )
        }
    }
}

@Composable
private fun AxisSelector(
    selectedAxis: MeasurementAxis,
    onAxisChange: (MeasurementAxis) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Axis: ",
            color = TextSecondary,
            fontSize = 14.sp
        )

        FilterChip(
            selected = selectedAxis == MeasurementAxis.PITCH,
            onClick = { if (enabled) onAxisChange(MeasurementAxis.PITCH) },
            label = { Text("Pitch (wheelie)") },
            enabled = enabled,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        FilterChip(
            selected = selectedAxis == MeasurementAxis.ROLL,
            onClick = { if (enabled) onAxisChange(MeasurementAxis.ROLL) },
            label = { Text("Roll (lean)") },
            enabled = enabled,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun AngleDisplay(
    angle: Float,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isRunning) getAngleColor(angle) else TextSecondary

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isRunning) "${abs(angle).toInt()}°" else "--",
                fontSize = 140.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )
            if (!isRunning) {
                Text(
                    text = "Press START to begin",
                    fontSize = 18.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StatsPanel(
    state: TiltState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.isInWheelie) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "WHEELIE!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = YellowAngle
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "Max",
                            value = "${state.currentWheelieMaxAngle.toInt()}°"
                        )
                        StatItem(
                            label = "Time",
                            value = formatDuration(state.currentWheelieDurationMs)
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Session Stats",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Wheelies",
                        value = "${state.wheelieCount}"
                    )
                    StatItem(
                        label = "Max Angle",
                        value = "${state.sessionMaxAngle.toInt()}°"
                    )
                    StatItem(
                        label = "Total Time",
                        value = formatDuration(state.sessionTotalDurationMs)
                    )
                }
            }
        }

        if (state.isTared) {
            Text(
                text = "TARED",
                fontSize = 12.sp,
                color = GreenAngle,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun ButtonPanel(
    isRunning: Boolean,
    isTared: Boolean,
    onStartStop: () -> Unit,
    onTare: () -> Unit,
    onResetTare: () -> Unit,
    onResetSession: () -> Unit,
    onToggleHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Big Start/Stop button
        Button(
            onClick = onStartStop,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) RedAngle else GreenAngle
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isRunning) "STOP" else "START",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tare buttons - only show when running
        if (isRunning) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTare,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTared) DarkSurface else GreenAngle
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "TARE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTared) TextSecondary else Color.White
                    )
                }

                Button(
                    onClick = onResetTare,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    enabled = isTared,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YellowAngle,
                        disabledContainerColor = DarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "RESET TARE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTared) DarkBackground else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Session controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onResetSession,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "NEW SESSION",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            OutlinedButton(
                onClick = onToggleHistory,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "HISTORY",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun HistoryScreen(
    sessions: List<WheelieSession>,
    onBack: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Session History",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Row {
                if (sessions.isNotEmpty()) {
                    TextButton(onClick = onClear) {
                        Text("Clear", color = RedAngle)
                    }
                }
                TextButton(onClick = onBack) {
                    Text("Back", color = GreenAngle)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No sessions yet",
                    fontSize = 16.sp,
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions.reversed()) { session ->
                    HistoryItem(session)
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(session: WheelieSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = session.formattedDate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = "${session.wheelieCount} wheelies",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${session.maxAngle.toInt()}°",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = getAngleColor(session.maxAngle)
                    )
                    Text(
                        text = "max",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = session.formattedDuration,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "total",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val seconds = ms / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return if (minutes > 0) {
        "${minutes}m ${remainingSeconds}s"
    } else {
        "${remainingSeconds}s"
    }
}
