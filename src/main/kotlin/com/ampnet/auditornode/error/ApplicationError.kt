package com.ampnet.auditornode.error

import arrow.core.Either

open class ApplicationError(val message: String, val cause: Throwable?) {
    override fun toString(): String = "${javaClass.simpleName}($message)"
}

typealias Try<T> = Either<ApplicationError, T>
