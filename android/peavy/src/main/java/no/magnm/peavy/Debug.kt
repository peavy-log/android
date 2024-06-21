package no.magnm.peavy

import android.util.Log

object Debug {
    var enabled: Boolean = false

    fun warn(message: String, throwable: Throwable? = null) {
        if (!enabled) return
        Log.w("Peavy", message, throwable)
    }
    fun log(message: String, throwable: Throwable? = null) {
        if (!enabled) return
        Log.d("Peavy", message, throwable)
    }
}