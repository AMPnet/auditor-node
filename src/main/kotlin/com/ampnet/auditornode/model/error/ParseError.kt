package com.ampnet.auditornode.model.error

sealed class ParseError(message: String, cause: Throwable? = null) : ApplicationError(message, cause) {

    data class JsonParseError(val json: String, override val cause: Throwable? = null) : ParseError(
        message = "Error parsing JSON value: $json",
        cause = cause
    )
}
