package peavy

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import peavy.options.PeavyOptions
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

internal class Push(private val options: PeavyOptions, private val storage: Storage) {
    @OptIn(ExperimentalCoroutinesApi::class)
    private var pusher = CoroutineScope(Dispatchers.IO.limitedParallelism(1))

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
        }.invokeOnCompletion {
            Debug.log("Pusher coroutine ended, $it")
        }
    }

    private suspend fun prepare(): Boolean {
        return if (storage.hasCurrentEntries()) {
            storage.endCurrentFile()
            true
        } else {
            false
        }
    }

    private suspend fun pushFiles() {
        var failures = 0
        storage.iterEndedFiles {
            if (push(it)) {
                it.delete()
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
            .post(file.asRequestBody(ndjson))
            .build()
        val response = try {
            okHttp.newCall(request).execute()
        } catch (e: Exception) {
            Debug.warn("Error pushing log file ${file.name}", e)
            null
        }
        return response?.use {
            Debug.log("Log ${file.name} pushed: ${it.code}")
            true
        } ?: false
    }
}