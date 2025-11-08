package peavy.constants

enum class EventResult {
    Success,
    Failure,
    Timeout,
    Cancelled;

    val stringValue: String
        get() = when (this) {
            Success -> "success"
            Failure -> "failure"
            Timeout -> "timeout"
            Cancelled -> "cancelled"
        }
}