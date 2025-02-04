package peavy.integration.timber

import android.util.Log
import peavy.Peavy
import timber.log.Timber

class PeavyTree : Timber.Tree() {
    private fun formatLogMessage(message: String, vararg args: Any?) = try {
        if (args.isNotEmpty()) {
            message.format(args)
        } else {
            message
        }
    } catch (e: Exception) {
        Log.e("PeavyTree", "Error formatting log message: $message", e)
        "<unknown message, formatting error>"
    }

    override fun v(t: Throwable?) = Peavy.t(t?.message ?: "<empty>", t)
    override fun v(message: String?, vararg args: Any?) {
        if (message.isNullOrEmpty()) return
        Peavy.t(formatLogMessage(message, *args))
    }

    override fun v(t: Throwable?, message: String?, vararg args: Any?) {
        if (message.isNullOrEmpty()) return
        Peavy.t(formatLogMessage(message, *args), t)
    }

    override fun d(t: Throwable?) = Peavy.d(t?.message ?: "<empty>", t)
    override fun d(message: String?, vararg args: Any?) {
        if (message.isNullOrEmpty()) return
        Peavy.d(formatLogMessage(message, *args))
    }

    override fun d(t: Throwable?, message: String?, vararg args: Any?) {
        if (message.isNullOrEmpty()) return
        Peavy.d(formatLogMessage(message, *args), t)
    }

    override fun i(t: Throwable?) = Peavy.i(t?.message ?: "<empty>", t)
    override fun i(message: String?, vararg args: Any?) {
        if (message.isNullOrEmpty()) return
        Peavy.i(formatLogMessage(message, *args))
    }

    override fun i(t: Throwable?, message: String?, vararg args: Any?) {
        if (message.isNullOrEmpty()) return
        Peavy.i(formatLogMessage(message, *args), t)
    }

    override fun w(t: Throwable?) = Peavy.w(t?.message ?: "<empty>", t)
    override fun w(message: String?, vararg args: Any?) {
        if (message.isNullOrEmpty()) return
        Peavy.w(formatLogMessage(message, *args))
    }

    override fun w(t: Throwable?, message: String?, vararg args: Any?) {
        if (message.isNullOrEmpty()) return
        Peavy.w(formatLogMessage(message, *args), t)
    }

    override fun e(t: Throwable?) = Peavy.e(t?.message ?: "<empty>", t)
    override fun e(message: String?, vararg args: Any?) {
        if (message.isNullOrEmpty()) return
        Peavy.e(formatLogMessage(message, *args))
    }

    override fun e(t: Throwable?, message: String?, vararg args: Any?) {
        if (message.isNullOrEmpty()) return
        Peavy.e(formatLogMessage(message, *args), t)
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        when (priority) {
            Log.INFO -> Peavy.i(message, t)
            Log.VERBOSE -> Peavy.d(message, t)
            Log.DEBUG -> Peavy.d(message, t)
            Log.WARN -> Peavy.w(message, t)
            Log.ERROR -> Peavy.e(message, t)
        }
    }

}