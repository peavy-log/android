package peavy.constants

enum class LogLevel(int: Int) {
    Trace(1),
    Debug(2),
    Info(3),
    Warning(4),
    Error(5);

    val stringValue: String
        get() = when (this) {
            Trace -> "trace"
            Debug -> "debug"
            Info -> "info"
            Warning -> "warning"
            Error -> "error"
        }
}