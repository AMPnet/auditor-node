package com.ampnet.auditornode.error

import kotlin.reflect.KClass

sealed class EvaluationError(message: String, cause: Throwable? = null) : ApplicationError(message, cause) {

    class InvalidReturnValueError(expectedType: KClass<*>) : EvaluationError(
        "Expected script to return value of type '${expectedType.qualifiedName}'"
    )

    class ScriptExecutionError(script: String, cause: Throwable?) : EvaluationError(
        message = "Error while executing provided script: $script",
        cause = cause
    )
}
