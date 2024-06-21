package no.magnm.peavy.options

import no.magnm.peavy.constants.LogLevel
import java.net.URL
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

data class PeavyOptions(
    val endpoint: URL,
    val logLevel: LogLevel = LogLevel.Info,
    val debug: Boolean = false,
    val pushInterval: Duration = 30.seconds,
    val maxHistoricalStorage: HistoricalStorage = HistoricalStorage(
        time = 7.days
    ),
) {
    data class HistoricalStorage(
        val time: Duration? = null,
        val size: Long? = null,
    )
}


