package no.magnm.peavy.exceptions

import no.magnm.peavy.constants.LogLevel

internal class VerbosityException(val level: LogLevel?, val minimum: LogLevel) : Exception()