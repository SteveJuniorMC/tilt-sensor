package com.tiltsensor.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiltsensor.data.WheelieSession
import com.tiltsensor.ui.theme.*
import kotlin.math.abs

data class TiltState(
    val angle: Float = 0f,
    val isTared: Boolean = false,
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
    onTare: () -> Unit,
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
                LandscapeLayout(state, onTare, onResetSession, onToggleHistory)
            } else {
                PortraitLayout(state, onTare, onResetSession, onToggleHistory)
            }
        }
    }
}

@Composable
private fun PortraitLayout(
    state: TiltState,
    onTare: () -> Unit,
    onResetSession: () -> Unit,
    onToggleHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.1f))

        AngleDisplay(
            angle = state.angle,
            modifier = Modifier.weight(0.4f)
        )

        StatsPanel(
            state = state,
            modifier = Modifier.weight(0.3f)
        )

        ButtonPanel(
            isTared = state.isTared,
            onTare = onTare,
            onResetSession = onResetSession,
            onToggleHistory = onToggleHistory,
            modifier = Modifier.weight(0.2f)
        )
    }
}

@Composable
private fun LandscapeLayout(
    state: TiltState,
    onTare: () -> Unit,
    onResetSession: () -> Unit,
    onToggleHistory: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AngleDisplay(
            angle = state.angle,
            modifier = Modifier
                .weight(0.5f)
                .fillMaxHeight()
        )

        Column(
            modifier = Modifier
                .weight(0.5f)
                .fillMaxHeight()
                .padding(start = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            StatsPanel(
                state = state,
                modifier = Modifier.weight(0.6f)
            )

            ButtonPanel(
                isTared = state.isTared,
                onTare = onTare,
                onResetSession = onResetSession,
                onToggleHistory = onToggleHistory,
                modifier = Modifier.weight(0.4f)
            )
        }
    }
}

@Composable
private fun AngleDisplay(
    angle: Float,
    modifier: Modifier = Modifier
) {
    val color = getAngleColor(angle)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${abs(angle).toInt()}",
                fontSize = 140.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )
            Text(
                text = "degrees",
                fontSize = 24.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
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
    isTared: Boolean,
    onTare: () -> Unit,
    onResetSession: () -> Unit,
    onToggleHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onTare,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isTared) DarkSurfaceVariant else GreenAngle
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isTared) "RESET TARE" else "TARE (SET LEVEL)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

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
