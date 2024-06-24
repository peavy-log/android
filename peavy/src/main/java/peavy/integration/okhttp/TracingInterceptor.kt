package peavy.integration.okhttp

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.buffer
import okio.sink
import org.json.JSONObject
import peavy.Peavy
import peavy.PeavyTrace
import peavy.PeavyTracing
import peavy.constants.LogLevel
import java.io.ByteArrayOutputStream

class PeavyTracingInterceptor(
    private val tracer: PeavyTracing = PeavyTracing.W3C(),
    private val includeBodies: Boolean = false,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val trace = tracer.newTrace()

        val tracedRequest = chain.request().newBuilder()
            .apply {
                tracer.requestHeaders(trace).forEach { (name, value) ->
                    addHeader(name, value)
                }
            }
            .build()

        logRequest(tracedRequest, trace)

        val response = chain.proceed(tracedRequest)

        logResponse(response, trace)

        return response
    }

    private fun logRequest(request: Request, trace: PeavyTrace) {
        Peavy.log {
            level = LogLevel.Info
            message = "HTTP Request ${request.method} ${request.url}"
            json = mapOf(
                "peavy/traceId" to trace.id,
                "peavy/spanId" to trace.span,
                "peavy/http" to JSONObject().apply {
                    put("side", "request")
                    put("url", request.url.toString())
                    put("method", request.method)
                    if (includeBodies && request.body != null) {
                        val length = request.body!!.contentLength()
                        if (length > 50_000) {
                            put("body", "<Truncated length=$length>")
                        } else {
                            put("body", readRequestBody(request.body!!))
                        }
                    }
                }
            )
        }
    }

    private fun logResponse(response: Response, trace: PeavyTrace) {
        Peavy.log {
            level = LogLevel.Info
            message =
                "HTTP Response ${response.code} ${response.request.method} ${response.request.url}"
            json = mapOf(
                "peavy/traceId" to trace.id,
                "peavy/spanId" to trace.span,
                "peavy/http" to JSONObject().apply {
                    put("side", "response")
                    put("url", response.request.url.toString())
                    put("method", response.request.method)
                    put("code", response.code)
                    put("success", response.isSuccessful)
                    put("rtt", response.receivedResponseAtMillis - response.sentRequestAtMillis)
                    if (includeBodies && response.body != null) {
                        val length = response.body!!.contentLength()
                        if (length > 50_000) {
                            put("body", "<Truncated length=$length>")
                        } else {
                            put("body", readResponseBody(response))
                        }
                    }
                }
            )
        }
    }

    private fun readRequestBody(body: RequestBody): String {
        return try {
            val outputStream = ByteArrayOutputStream()
            val sink = outputStream.sink().buffer()
            body.writeTo(sink)
            outputStream.toByteArray().decodeToString(throwOnInvalidSequence = true)
        } catch (e: Exception) {
            "<Binary length=${body.contentLength()}>"
        }
    }

    private fun readResponseBody(response: Response): String {
        return try {
            val peekedBody = response.peekBody(50_000)
            peekedBody.bytes().decodeToString(throwOnInvalidSequence = true)
        } catch (e: CharacterCodingException) {
            "<Binary length=${response.body?.contentLength() ?: -1}>"
        } catch (e: Exception) {
            "<Unreadable body>"
        }
    }
}