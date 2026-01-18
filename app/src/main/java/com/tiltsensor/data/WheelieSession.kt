package com.tiltsensor.data

data class WheelieSession(
    val timestamp: Long = System.currentTimeMillis(),
    val maxAngle: Float = 0f,
    val wheelieCount: Int = 0,
    val totalDurationMs: Long = 0
) {
    val formattedDate: String
        get() {
            val date = java.util.Date(timestamp)
            val format = java.text.SimpleDateFormat("MMM d, HH:mm", java.util.Locale.getDefault())
            return format.format(date)
        }

    val formattedDuration: String
        get() {
            val seconds = totalDurationMs / 1000
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return if (minutes > 0) {
                "${minutes}m ${remainingSeconds}s"
            } else {
                "${remainingSeconds}s"
            }
        }
}
