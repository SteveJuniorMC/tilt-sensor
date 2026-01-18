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

class TiltSensor(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _angle = MutableStateFlow(0f)
    val angle: StateFlow<Float> = _angle.asStateFlow()

    private var tareOffset = 0f
    private var filteredY = 0f
    private var filteredZ = 0f
    private var initialized = false

    companion object {
        private const val LOW_PASS_ALPHA = 0.15f
    }

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    fun tare() {
        tareOffset = calculateRawAngle()
    }

    fun resetTare() {
        tareOffset = 0f
    }

    private fun calculateRawAngle(): Float {
        val radians = atan2(filteredY, filteredZ)
        return Math.toDegrees(radians.toDouble()).toFloat()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val y = event.values[1]
            val z = event.values[2]

            if (!initialized) {
                filteredY = y
                filteredZ = z
                initialized = true
            } else {
                filteredY = lowPassFilter(y, filteredY)
                filteredZ = lowPassFilter(z, filteredZ)
            }

            val rawAngle = calculateRawAngle()
            _angle.value = rawAngle - tareOffset
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    private fun lowPassFilter(input: Float, previous: Float): Float {
        return previous + LOW_PASS_ALPHA * (input - previous)
    }
}
