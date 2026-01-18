package com.tiltsensor.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class SessionRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "tilt_sensor_prefs"
        private const val KEY_SESSIONS = "sessions"
        private const val MAX_SESSIONS = 50
    }

    fun saveSessions(sessions: List<WheelieSession>) {
        val jsonArray = JSONArray()
        sessions.takeLast(MAX_SESSIONS).forEach { session ->
            val jsonObject = JSONObject().apply {
                put("timestamp", session.timestamp)
                put("maxAngle", session.maxAngle.toDouble())
                put("wheelieCount", session.wheelieCount)
                put("totalDurationMs", session.totalDurationMs)
            }
            jsonArray.put(jsonObject)
        }
        prefs.edit().putString(KEY_SESSIONS, jsonArray.toString()).apply()
    }

    fun loadSessions(): List<WheelieSession> {
        val jsonString = prefs.getString(KEY_SESSIONS, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonString)
            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                WheelieSession(
                    timestamp = obj.getLong("timestamp"),
                    maxAngle = obj.getDouble("maxAngle").toFloat(),
                    wheelieCount = obj.getInt("wheelieCount"),
                    totalDurationMs = obj.getLong("totalDurationMs")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveSession(session: WheelieSession) {
        val sessions = loadSessions().toMutableList()
        sessions.add(session)
        saveSessions(sessions)
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_SESSIONS).apply()
    }
}
