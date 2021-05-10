package com.ampnet.auditornode.model.error

import com.ampnet.auditornode.script.api.model.AuditResult

sealed class EvaluationError(message: String, cause: Throwable? = null) : ApplicationError(message, cause) {

    data class InvalidReturnValueError(val actualType: String) : EvaluationError(
        "Expected script to return value of type '${AuditResult::class.qualifiedName}' but was '$actualType'"
    ) {
        constructor(actualClass: Class<*>) : this(actualClass.name ?: "unknown")
    }

    data class InvalidInputValueError(
        val methodCall: String,
        val argumentIndex: Int,
        val expectedType: String,
        val actualType: String
    ) : EvaluationError(
        "Method call $methodCall expects '$expectedType' as argument with index $argumentIndex but was '$actualType'"
    )

    data class ScriptExecutionError(val script: String, override val cause: Throwable?) : EvaluationError(
        message = "Error while executing provided script: $script",
        cause = cause
    )
}
