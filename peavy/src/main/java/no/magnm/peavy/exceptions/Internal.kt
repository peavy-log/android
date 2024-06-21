package no.magnm.peavy.exceptions

import no.magnm.peavy.Peavy

internal object Internal {
    fun attachUncaughtHandler() {
        val origHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            Peavy.storage.flushImmediately()
            origHandler?.uncaughtException(t, e)
        }
    }
}