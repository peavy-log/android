package peavy

import peavy.constants.EventResult
import peavy.constants.EventState
import peavy.constants.EventType
import peavy.constants.LogLevel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


fun Peavy.ev(
    type: EventType,
    category: String,
    name: String,
    ident: String = "",
    duration: Duration = 0.seconds,
    result: EventResult = EventResult.Success
) {
    logger.log {
        level = LogLevel.Info
        json = mapOf(
            "__peavy_type" to "event",
            "message" to "", // empty out the default message
            "type" to type.stringValue,
            "category" to category,
            "name" to name,
            "ident" to ident,
            "duration" to duration.inWholeMilliseconds,
            "result" to result.stringValue,
        )
    }
}

fun Peavy.action(
    category: String,
    name: String,
    ident: String = "",
    duration: Duration = 0.seconds,
    result: EventResult = EventResult.Success
) {
    ev(
        type = EventType.Action,
        category = category,
        name = name,
        ident = ident,
        duration = duration,
        result = result
    )
}

fun Peavy.action(
    category: String, name: String, ident: Int, duration: Duration = 0.seconds,
    result: EventResult = EventResult.Success
) {
    ev(
        type = EventType.Action,
        category = category,
        name = name,
        ident = ident.toString(),
        duration = duration,
        result = result
    )
}

fun Peavy.action(
    category: String, name: String, ident: Long, duration: Duration = 0.seconds,
    result: EventResult = EventResult.Success
) {
    ev(
        type = EventType.Action,
        category = category,
        name = name,
        ident = ident.toString(),
        duration = duration,
        result = result
    )
}

fun Peavy.action(
    category: String, name: String, ident: Float, duration: Duration = 0.seconds,
    result: EventResult = EventResult.Success
) {
    ev(
        type = EventType.Action,
        category = category,
        name = name,
        ident = ident.toString(),
        duration = duration,
        result = result
    )
}

fun Peavy.action(
    category: String, name: String, ident: Boolean, duration: Duration = 0.seconds,
    result: EventResult = EventResult.Success
) {
    ev(
        type = EventType.Action,
        category = category,
        name = name,
        ident = ident.toString(),
        duration = duration,
        result = result
    )
}

fun Peavy.state(name: EventState, value: String) {
    ev(type = EventType.State, category = "device", name = name.stringValue, ident = value)
}

fun Peavy.state(name: EventState, value: Int) {
    ev(
        type = EventType.State,
        category = "device",
        name = name.stringValue,
        ident = value.toString()
    )
}

fun Peavy.state(name: EventState, value: Long) {
    ev(
        type = EventType.State,
        category = "device",
        name = name.stringValue,
        ident = value.toString()
    )
}

fun Peavy.state(name: EventState, value: Float) {
    ev(
        type = EventType.State,
        category = "device",
        name = name.stringValue,
        ident = value.toString()
    )
}

fun Peavy.state(name: EventState, value: Boolean) {
    ev(
        type = EventType.State,
        category = "device",
        name = name.stringValue,
        ident = value.toString()
    )
}