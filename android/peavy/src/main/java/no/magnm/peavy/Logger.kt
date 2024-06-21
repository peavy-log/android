package no.magnm.peavy

import android.content.Context
import android.os.Build
import no.magnm.peavy.exceptions.VerbosityException
import no.magnm.peavy.options.PeavyOptions

internal class Logger(
    context: Context,
    private val options: PeavyOptions,
    private val storage: Storage,
) {
    val meta = mutableMapOf<String, Any?>()
    private val labels = mutableMapOf<String, Any?>()

    init {
        generateGlobalLabels(context)
    }

    fun log(closure: LogEntryBuilder.() -> Unit) {
        val entry = buildEntry(closure) ?: return
        storage.storeEntry(entry)
    }

    private fun buildEntry(closure: LogEntryBuilder.() -> Unit) = try {
        val builder = LogEntryBuilder(options.logLevel)
        closure(builder)
        builder.build().apply {
            labels.putAll(this@Logger.labels)
            labels.putAll(this@Logger.meta)
        }
    } catch (e: VerbosityException) {
        Debug.log("Discarded log line with level ${e.level} due to verbosity level (${e.minimum})")
        null
    }

    private fun generateGlobalLabels(context: Context) {
        val appVersion = getAppVersion(context)

        labels.apply {
            put("peavy-version", BuildConfig.VERSION)
            put("platform", "android")
            put("platform-version", Build.VERSION.SDK_INT)
            put("app-id", context.packageName)
            if (appVersion != null) {
                put("app-version", appVersion.first)
                put("app-version-code", appVersion.second)
            }
            put("device-model", "${Build.MANUFACTURER} ${Build.MODEL}")
            put("device-language", context.resources.configuration.locale.let { "${it.language}-${it.country}" })
            put("device-screen-w", context.resources.configuration.screenWidthDp)
            put("device-screen-h", context.resources.configuration.screenHeightDp)
        }
    }

    private fun getAppVersion(context: Context): Pair<String, Int>? {
        return try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            Pair(info.versionName, info.versionCode)
        } catch (e: Exception) {
            Debug.warn("Error fetching app info", e)
            null
        }
    }
}