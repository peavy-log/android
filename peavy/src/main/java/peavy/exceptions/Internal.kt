package peavy.exceptions

import peavy.Peavy

internal object Internal {
    fun attachUncaughtHandler() {
        val origHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            Peavy.storage.flushImmediately()
            origHandler?.uncaughtException(t, e)
        }
    }
}