package com.ampnet.auditornode.model.error

import arrow.core.Either

abstract class ApplicationError(message: String, cause: Throwable?) : RuntimeException(message, cause) {
    final override fun toString(): String = "${javaClass.simpleName}($message)"
}

typealias Try<T> = Either<ApplicationError, T>
