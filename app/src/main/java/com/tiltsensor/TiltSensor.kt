package com.tiltsensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2

enum class MeasurementAxis {
    PITCH,  // Forward/back tilt (Y/Z) - for wheelies with phone portrait
    ROLL    // Left/right tilt (X/Z) - for lean angle
}

class TiltSensor(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _angle = MutableStateFlow(0f)
    val angle: StateFlow<Float> = _angle.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var tareOffset = 0f
    private var isTared = false

    private var filteredX = 0f
    private var filteredY = 0f
    private var filteredZ = 0f
    private var initialized = false

    var axis: MeasurementAxis = MeasurementAxis.PITCH

    companion object {
        // Lower alpha = more smoothing, less responsive
        private const val LOW_PASS_ALPHA = 0.05f
    }

    fun start() {
        if (_isRunning.value) return

        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
            _isRunning.value = true
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        _isRunning.value = false
        _angle.value = 0f
        initialized = false
    }

    fun tare() {
        tareOffset = calculateRawAngle()
        isTared = true
    }

    fun resetTare() {
        tareOffset = 0f
        isTared = false
    }

    fun isTared(): Boolean = isTared

    private fun calculateRawAngle(): Float {
        val radians = when (axis) {
            MeasurementAxis.PITCH -> atan2(filteredY, filteredZ)
            MeasurementAxis.ROLL -> atan2(filteredX, filteredZ)
        }
        return Math.toDegrees(radians.toDouble()).toFloat()
    }

    private fun normalizeAngle(angle: Float): Float {
        var normalized = angle
        while (normalized > 180f) normalized -= 360f
        while (normalized < -180f) normalized += 360f
        return normalized
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER && _isRunning.value) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            if (!initialized) {
                filteredX = x
                filteredY = y
                filteredZ = z
                initialized = true
            } else {
                filteredX = lowPassFilter(x, filteredX)
                filteredY = lowPassFilter(y, filteredY)
                filteredZ = lowPassFilter(z, filteredZ)
            }

            val rawAngle = calculateRawAngle()
            val adjustedAngle = normalizeAngle(rawAngle - tareOffset)
            _angle.value = adjustedAngle
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    private fun lowPassFilter(input: Float, previous: Float): Float {
        return previous + LOW_PASS_ALPHA * (input - previous)
    }
}
