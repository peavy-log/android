package peavy.exceptions

import peavy.constants.LogLevel

internal class VerbosityException(val level: LogLevel?, val minimum: LogLevel) : Exception()