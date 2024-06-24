package peavy

import java.util.UUID

data class PeavyTrace(
    val id: String,
    val span: String,
    val full: String
)

abstract class PeavyTracing {
    abstract fun newTrace(spanId: String = ""): PeavyTrace
    abstract fun requestHeaders(trace: PeavyTrace): Map<String, String>

    fun newId(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    open class W3C : PeavyTracing() {
        override fun newTrace(spanId: String): PeavyTrace {
            val traceId = newId()
            val spanId = spanId.ifEmpty { newId().takeLast(16) }
            return PeavyTrace(
                id = traceId,
                span = spanId,
                full = "00-${traceId}-${spanId}-03"
            )
        }

        override fun requestHeaders(trace: PeavyTrace): Map<String, String> {
            return mapOf("traceparent" to trace.full)
        }
    }

    object GoogleCloud : W3C() {
        override fun requestHeaders(trace: PeavyTrace): Map<String, String> {
            return super.requestHeaders(trace).toMutableMap().apply {
                put("x-request-id", trace.id)
                put("x-cloud-trace-context", "${trace.id}/0;o=1")
            }
        }
    }
}



