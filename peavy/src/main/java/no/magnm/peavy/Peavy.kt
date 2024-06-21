package no.magnm.peavy

import android.content.Context
import no.magnm.peavy.constants.LogLevel
import no.magnm.peavy.exceptions.Internal
import no.magnm.peavy.options.PeavyOptions

object Peavy {
    private lateinit var logger: Logger
    internal lateinit var storage: Storage
    private lateinit var push: Push

    fun init(context: Context, options: PeavyOptions) {
        Debug.enabled = options.debug

        storage = Storage(context)
        logger = Logger(context, options, storage)
        push = Push(options, storage)

        Internal.attachUncaughtHandler()
    }

    fun clearMeta() {
        logger.meta.clear()
    }

    fun setMeta(vararg metas: Pair<String, Any?>) {
        for (meta in metas) {
            logger.meta[meta.first] = meta.second
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