package peavy

import android.content.Context
import android.os.Build
import android.util.Log
import peavy.constants.LogLevel
import peavy.exceptions.VerbosityException
import peavy.options.PeavyOptions
import java.util.UUID

internal class Logger(
    context: Context,
    var options: PeavyOptions,
    private val storage: Storage,
) {
    val meta = mutableMapOf<String, Any?>()
    private val logLabels = mutableMapOf<String, Any?>()
    private val evLabels = mutableMapOf<String, Any?>()

    init {
        generateGlobalLabels(context)
        resetSessionId()
    }

    fun log(closure: LogEntryBuilder.() -> Unit) {
        val entry = buildEntry(closure) ?: return
        storage.storeEntry(entry)
        if (options.printToStdout) {
            logToStdout(entry)
        }
    }

    internal fun buildEntry(closure: LogEntryBuilder.() -> Unit) = try {
        val builder = LogEntryBuilder(options.logLevel)
        closure(builder)
        builder.build().apply {
            if (builder.json?.get("__peavy_type") == "event") {
                labels.putAll(this@Logger.evLabels)
            } else {
                labels.putAll(this@Logger.logLabels)

            }
            labels.putAll(this@Logger.meta)
        }
    } catch (e: VerbosityException) {
        Debug.log("Discarded log line with level ${e.level} due to verbosity level (${e.minimum})")
        null
    }

    private fun logToStdout(entry: LogEntry) = when (entry.level) {
        LogLevel.Trace -> Log.v(null, entry.message)
        LogLevel.Debug -> Log.d(null, entry.message)
        LogLevel.Info -> Log.i(null, entry.message)
        LogLevel.Warning -> Log.w(null, entry.message)
        LogLevel.Error -> Log.e(null, entry.message)
    }

    private fun generateGlobalLabels(context: Context) {
        val appVersion = getAppVersion(context)

        logLabels.apply {
            put("peavy-version", BuildConfig.VERSION)
            put("platform", "android")
            put("platform-version", Build.VERSION.SDK_INT)
            put("app-id", context.packageName)
            if (appVersion != null) {
                put("app-version", appVersion.first)
                put("app-version-code", appVersion.second)
            }
            put("device-model", "${Build.MANUFACTURER} ${Build.MODEL}")
            put(
                "device-language",
                context.resources.configuration.locale.let { "${it.language}-${it.country}" })
            put("device-screen-w", context.resources.configuration.screenWidthDp)
            put("device-screen-h", context.resources.configuration.screenHeightDp)
        }
        evLabels.apply {
            put("platform", "android")
            put("app-id", context.packageName)
        }
    }

    internal fun resetSessionId() {
        val id = UUID.randomUUID().toString().replace("-", "").take(24)
        Debug.log("Reset session id to $id")
        logLabels.apply {
            put("session-id", id)
        }
        evLabels.apply {
            put("session-id", id)
        }
    }

    private fun getAppVersion(context: Context): Pair<String, Int>? {
        return try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            Pair(info.versionName ?: "unknown", info.versionCode)
        } catch (e: Exception) {
            Debug.warn("Error fetching app info", e)
            null
        }
    }
}