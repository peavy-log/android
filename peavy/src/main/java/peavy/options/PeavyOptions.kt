package peavy.options

import peavy.constants.LogLevel
import java.net.URL
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

data class PeavyOptions(
    /**
     * The remote endpoint to push logs to.
     * Should be a full URL.
     */
    val endpoint: URL,
    /**
     * Minimum log level to process.
     *
     * Default: LogLevel.Info
     */
    val logLevel: LogLevel = LogLevel.Info,
    /**
     * Enable Peavy to also print the log line to stdout
     * (using builtin android.Log)
     *
     * If Peavy is being used directly from within code
     * (ie. not through f.ex. Timber), then you probably want
     * this enabled in debug builds.
     */
    val printToStdout: Boolean = false,
    /**
     * Whether to enable library debug mode.
     * This enables logging (to stdout only) of local Peavy actions
     *
     * Default: false
     */
    val debug: Boolean = false,
    /**
     * How often to push logs to remote.
     * Logs are cached locally on device, and sent once every pushInterval.
     *
     * Default: 30 seconds
     */
    val pushInterval: Duration = 30.seconds,
    /**
     * How many logs to store locally on device if pushing is failing
     * (for example when device is offline).
     *
     * Default: 7 days
     */
    val maxHistoricalStorage: HistoricalStorage = HistoricalStorage(
        time = 7.days
    ),
    /**
     * Attach a handler for uncaught exceptions to immediately flush logs up to and
     * including the crash. It's not turned on by default because f.ex. Crashlytics
     * is usually installed too, and does it better.
     *
     * Default: false
     */
    val attachUncaughtHandler: Boolean = false,
) {
    data class HistoricalStorage(
        /**
         * Prune historical storage based on time.
         * Any logs older than this time will be deleted.
         */
        val time: Duration? = null,
        /**
         * Prune historical storage based on size.
         * When total size of all stored logs exceed
         * this size, the oldest entries will be deleted.
         *
         * It is generally unnecessary to set this, because
         * logs compress very well on-device, and it'll take
         * _a lot_ of time to reach sizes that need pruning.
         */
        val size: Long? = null,
    )
}


