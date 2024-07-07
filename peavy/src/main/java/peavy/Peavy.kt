package peavy

import android.content.Context
import android.content.SharedPreferences
import peavy.constants.LogLevel
import peavy.exceptions.Internal
import peavy.options.PeavyOptions

object Peavy {
    private lateinit var logger: Logger
    internal lateinit var storage: Storage
    private lateinit var push: Push
    private lateinit var prefs: SharedPreferences

    fun init(context: Context, options: PeavyOptions) {
        Debug.enabled = options.debug

        prefs = context.getSharedPreferences("__peavy_meta", Context.MODE_PRIVATE)
        storage = Storage(context)
        logger = Logger(context, options, storage)
        push = Push(options, storage)

        Internal.attachUncaughtHandler()
        restoreMeta()
    }

    fun clearMeta() {
        logger.meta.clear()
        prefs.edit().clear().apply()
    }

    fun setMeta(vararg metas: Pair<String, Any?>) {
        val prefEdit = prefs.edit()
        for (meta in metas) {
            when (meta.second) {
                null -> prefEdit.remove(meta.first)
                is String -> prefEdit.putString(meta.first, meta.second as String)
                is Int -> prefEdit.putInt(meta.first, meta.second as Int)
                is Float -> prefEdit.putFloat(meta.first, meta.second as Float)
                is Long -> prefEdit.putLong(meta.first, meta.second as Long)
                is Boolean -> prefEdit.putBoolean(meta.first, meta.second as Boolean)
                else -> throw IllegalArgumentException("Invalid value type for key ${meta.first}")
            }
            if (meta.second == null) {
                logger.meta.remove(meta.first)
            } else {
                logger.meta[meta.first] = meta.second
            }
        }
        prefEdit.apply()
    }

    private fun restoreMeta() {
        prefs.all.entries.forEach {
            logger.meta[it.key] = it.value
        }
    }

    fun log(closure: LogEntryBuilder.() -> Unit) {
        logger.log(closure)
    }

    fun t(message: String) = t(message, null)
    fun t(message: String, throwable: Throwable?) {
        logger.log {
            this.level = LogLevel.Trace
            this.message = message
            this.throwable = throwable
        }
    }

    fun d(message: String) = d(message, null)
    fun d(message: String, throwable: Throwable?) {
        logger.log {
            this.level = LogLevel.Debug
            this.message = message
            this.throwable = throwable
        }
    }

    fun i(message: String) = i(message, null)
    fun i(message: String, throwable: Throwable?) {
        logger.log {
            this.level = LogLevel.Info
            this.message = message
            this.throwable = throwable
        }
    }

    fun w(message: String) = w(message, null)
    fun w(message: String, throwable: Throwable?) {
        logger.log {
            this.level = LogLevel.Warning
            this.message = message
            this.throwable = throwable
        }
    }

    fun e(message: String) = e(message, null)
    fun e(message: String, throwable: Throwable?) {
        logger.log {
            this.level = LogLevel.Error
            this.message = message
            this.throwable = throwable
        }
    }
}