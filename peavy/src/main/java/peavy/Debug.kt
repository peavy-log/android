package peavy

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import peavy.constants.LogLevel
import java.time.Instant

internal object Debug {
    var enabled: Boolean = false

    private val warnScope = CoroutineScope(Dispatchers.IO)
    private var lastSomeWarn = Instant.ofEpochSecond(0)

    fun warnSome(message: String, throwable: Throwable? = null) {
        if (lastSomeWarn.isAfter(Instant.now().minusSeconds(60))) {
            return
        }
        lastSomeWarn = Instant.now()
        warn(message, throwable)
    }

    fun warn(message: String, throwable: Throwable? = null) {
        warnScope.launch {
            if (!Peavy.isInitialized) return@launch

            try {
                val entry = Peavy.logger.buildEntry {
                    this.level = LogLevel.Warning
                    this.message = message
                    this.throwable = throwable
                }?.apply {
                    labels["peavy/internal"] = "true"
                } ?: return@launch

                if (!Peavy.push.push(entry)) {
                    // Failed to direct push the entry. Queue it to normal logger instead
                    Peavy.storage.storeEntry(entry)
                }
            } catch (e: Exception) {
                // Nothing we can do at this point, swallow it
            }
        }

        Log.w("Peavy", message, throwable)
    }

    fun log(message: String, throwable: Throwable? = null) {
        if (!enabled) return
        Log.d("Peavy", message, throwable)
    }
}