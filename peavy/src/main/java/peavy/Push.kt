package peavy

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import peavy.options.PeavyOptions
import java.io.File
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

internal class Push(var options: PeavyOptions, private val storage: Storage) {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val pusher = CoroutineScope(Dispatchers.IO.limitedParallelism(1))
    private val pushMutex = Mutex()

    private var okHttp: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10.seconds.toJavaDuration())
        .callTimeout(30.seconds.toJavaDuration())
        .writeTimeout(30.seconds.toJavaDuration())
        .build()
    private val ndjson = "application/ndjson".toMediaType()

    init {
        pusher.launch {
            while (true) {
                delay(options.pushInterval)
                prepareAndPush()
            }
        }.invokeOnCompletion {
            Debug.log("Pusher coroutine ended, $it")
        }
    }

    internal suspend fun prepareAndPush() {
        pushMutex.withLock {
            try {
                withTimeout(1.minutes) {
                    if (prepare()) {
                        pushFiles()
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Debug.warn("Timeout pushing")
            }
        }
    }

    private suspend fun prepare(): Boolean {
        storage.flush()

        return if (storage.hasCurrentEntries()) {
            try {
                storage.endCurrentFile()
                true
            } catch (e: Exception) {
                Debug.warn("Error rolling current", e)
                false
            }
        } else {
            false
        }
    }

    private suspend fun pushFiles() {
        var failures = 0
        storage.iterEndedFiles {
            if (push(it)) {
                try {
                    it.delete()
                } catch (e: Exception) {
                    Debug.warn("Error deleting file", e)
                }
            } else {
                failures += 1
            }

            return@iterEndedFiles failures < 3
        }
    }

    private fun push(file: File): Boolean {
        Debug.log("Pushing ${file.name}, size ${file.length()}")
        val request = Request.Builder()
            .url(options.endpoint)
            .header("Content-Encoding", "gzip")
            .header("Peavy-Log", "true")
            .post(file.asRequestBody(ndjson))
            .build()
        val response = try {
            okHttp.newCall(request).execute().also {
                if (it.code >= 400) {
                    throw Exception("Push error: ${it.code}")
                }
            }
        } catch (e: Exception) {
            Debug.warn("Error pushing log file ${file.name}", e)
            null
        }
        return response?.use {
            Debug.log("Log ${file.name} pushed: ${it.code}")
            true
        } ?: false
    }

    internal fun push(entry: LogEntry): Boolean {
        val json = entry.toJson().toString()
        Debug.log("Pushing entry, size ${json.length}")
        val request = Request.Builder()
            .url(options.endpoint)
            .header("Peavy-Log", "true")
            .post(json.toRequestBody(ndjson))
            .build()
        val response = try {
            okHttp.newCall(request).execute().also {
                if (it.code >= 400) {
                    throw Exception("Push error: ${it.code}")
                }
            }
        } catch (e: Exception) {
            null
        }
        return response?.use {
            Debug.log("Log entry pushed: ${it.code}")
            true
        } ?: false
    }
}