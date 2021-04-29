package com.ampnet.auditornode.error

import arrow.core.Either

abstract class Error(val message: String, val cause: Throwable?) {
    override fun toString(): String = "${javaClass.simpleName}($message)"
}

typealias Try<T> = Either<Error, T>
