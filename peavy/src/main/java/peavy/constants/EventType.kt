package peavy.constants

enum class EventType {
    State,
    Action;

    val stringValue: String
        get() = when (this) {
            State -> "state"
            Action -> "action"
        }
}