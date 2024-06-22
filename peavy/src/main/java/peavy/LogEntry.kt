package peavy

import peavy.constants.LogLevel
import peavy.exceptions.VerbosityException
import org.json.JSONObject
import java.time.Instant
import kotlin.properties.Delegates

internal data class LogEntry(
    val timestamp: Instant,
    val level: LogLevel,
    val message: String,
    val throwable: Throwable? = null,
    val json: Map<String, JSONObject>? = null,
    val labels: MutableMap<String, Any?> = mutableMapOf(),
) {
    fun toJson() = JSONObject().apply {
        throwable?.let {
            put("error", it.stackTraceToString())
        }

        val jsonLabels = JSONObject().apply {
            labels.entries.forEach {
                if (it.value != null) {
                    put(it.key, it.value)
                }
            }
        }
        put("peavy/labels", jsonLabels)


        put("timestamp", timestamp.toString())
        put("severity", level.stringValue)
        put("message", message.ifEmpty {
            level.stringValue
        })
        json?.entries?.forEach {
            put(it.key, it.value)
        }
    }
}

class LogEntryBuilder(private val minimumLevel: LogLevel) {
    var level: LogLevel? by Delegates.vetoable(null) { _, _, value ->
        if (value == null || value < minimumLevel) {
            throw VerbosityException(value, minimumLevel)
        }
        true
    }
    var message: String = ""
    var throwable: Throwable? = null
    var json: Map<String, JSONObject>? = null

    internal fun build() = LogEntry(
        timestamp = Instant.now(),
        message = message,
        level = level ?: throw VerbosityException(null, minimumLevel),
        json = json,
        throwable = throwable,
    )
}