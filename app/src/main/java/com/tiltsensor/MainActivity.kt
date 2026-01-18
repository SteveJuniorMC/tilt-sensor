package com.tiltsensor

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.tiltsensor.data.SessionRepository
import com.tiltsensor.data.WheelieSession
import com.tiltsensor.ui.TiltScreen
import com.tiltsensor.ui.TiltState
import com.tiltsensor.ui.theme.TiltSensorTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.abs
import kotlin.math.max

class MainActivity : ComponentActivity() {

    private lateinit var tiltSensor: TiltSensor
    private lateinit var sessionRepository: SessionRepository

    companion object {
        private const val WHEELIE_THRESHOLD = 15f
    }

    private var isRunning = mutableStateOf(false)
    private var isTared = mutableStateOf(false)
    private var selectedAxis = mutableStateOf(MeasurementAxis.PITCH)
    private var currentAngle = mutableStateOf(0f)
    private var sessionMaxAngle = mutableStateOf(0f)
    private var currentWheelieMaxAngle = mutableStateOf(0f)
    private var wheelieCount = mutableStateOf(0)
    private var currentWheelieDurationMs = mutableStateOf(0L)
    private var sessionTotalDurationMs = mutableStateOf(0L)
    private var isInWheelie = mutableStateOf(false)
    private var showHistory = mutableStateOf(false)
    private var history = mutableStateOf<List<WheelieSession>>(emptyList())

    private var wheelieStartTime: Long = 0
    private var lastUpdateTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on while app is running
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        tiltSensor = TiltSensor(this)
        sessionRepository = SessionRepository(this)
        history.value = sessionRepository.loadSessions()

        tiltSensor.angle.onEach { angle ->
            if (isRunning.value) {
                updateAngle(angle)
            }
        }.launchIn(lifecycleScope)

        tiltSensor.isRunning.onEach { running ->
            isRunning.value = running
        }.launchIn(lifecycleScope)

        setContent {
            TiltSensorTheme {
                val state = TiltState(
                    angle = currentAngle.value,
                    isTared = isTared.value,
                    isRunning = isRunning.value,
                    selectedAxis = selectedAxis.value,
                    sessionMaxAngle = sessionMaxAngle.value,
                    currentWheelieMaxAngle = currentWheelieMaxAngle.value,
                    wheelieCount = wheelieCount.value,
                    currentWheelieDurationMs = currentWheelieDurationMs.value,
                    sessionTotalDurationMs = sessionTotalDurationMs.value,
                    isInWheelie = isInWheelie.value,
                    history = history.value,
                    showHistory = showHistory.value
                )

                TiltScreen(
                    state = state,
                    onStartStop = { handleStartStop() },
                    onTare = { handleTare() },
                    onResetTare = { handleResetTare() },
                    onAxisChange = { axis -> handleAxisChange(axis) },
                    onResetSession = { handleResetSession() },
                    onToggleHistory = { showHistory.value = !showHistory.value },
                    onClearHistory = { handleClearHistory() }
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()

        if (isRunning.value) {
            if (isInWheelie.value) {
                endWheelie()
            }
            // Don't stop the sensor, just save current session
            saveCurrentSession()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tiltSensor.stop()
    }

    private fun handleStartStop() {
        if (isRunning.value) {
            // Stopping
            if (isInWheelie.value) {
                endWheelie()
            }
            tiltSensor.stop()
            currentAngle.value = 0f
        } else {
            // Starting
            lastUpdateTime = System.currentTimeMillis()
            tiltSensor.start()
        }
    }

    private fun updateAngle(angle: Float) {
        currentAngle.value = angle
        val absAngle = abs(angle)
        val currentTime = System.currentTimeMillis()

        sessionMaxAngle.value = max(sessionMaxAngle.value, absAngle)

        if (absAngle >= WHEELIE_THRESHOLD) {
            if (!isInWheelie.value) {
                startWheelie()
            } else {
                val elapsed = currentTime - lastUpdateTime
                currentWheelieDurationMs.value += elapsed
                sessionTotalDurationMs.value += elapsed
            }
            currentWheelieMaxAngle.value = max(currentWheelieMaxAngle.value, absAngle)
        } else {
            if (isInWheelie.value) {
                endWheelie()
            }
        }

        lastUpdateTime = currentTime
    }

    private fun startWheelie() {
        isInWheelie.value = true
        wheelieStartTime = System.currentTimeMillis()
        currentWheelieMaxAngle.value = abs(currentAngle.value)
        currentWheelieDurationMs.value = 0
    }

    private fun endWheelie() {
        isInWheelie.value = false
        wheelieCount.value++
        // Reset current wheelie stats but keep session stats
        currentWheelieMaxAngle.value = 0f
        currentWheelieDurationMs.value = 0
    }

    private fun handleTare() {
        tiltSensor.tare()
        isTared.value = true
    }

    private fun handleResetTare() {
        tiltSensor.resetTare()
        isTared.value = false
    }

    private fun handleAxisChange(axis: MeasurementAxis) {
        selectedAxis.value = axis
        tiltSensor.axis = axis
        // Reset tare when axis changes
        handleResetTare()
    }

    private fun handleResetSession() {
        saveCurrentSession()

        sessionMaxAngle.value = 0f
        wheelieCount.value = 0
        sessionTotalDurationMs.value = 0
        currentWheelieMaxAngle.value = 0f
        currentWheelieDurationMs.value = 0
        isInWheelie.value = false
        // Don't reset tare - keep it
    }

    private fun handleClearHistory() {
        sessionRepository.clearHistory()
        history.value = emptyList()
    }

    private fun saveCurrentSession() {
        if (wheelieCount.value > 0 || sessionMaxAngle.value > WHEELIE_THRESHOLD) {
            val session = WheelieSession(
                timestamp = System.currentTimeMillis(),
                maxAngle = sessionMaxAngle.value,
                wheelieCount = wheelieCount.value,
                totalDurationMs = sessionTotalDurationMs.value
            )
            sessionRepository.saveSession(session)
            history.value = sessionRepository.loadSessions()
        }
    }
}
