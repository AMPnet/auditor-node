package com.ampnet.auditornode

import arrow.core.Either
import assertk.Assert
import assertk.assertions.isEqualTo
import assertk.assertions.support.expected

fun <A, B> Assert<Either<A, B>>.isLeftSatisfying(assertions: (A) -> Unit) = given { actual ->
    if (actual is Either.Left) {
        assertions(actual.value)
    } else {
        expected("$actual to be Either.Left")
    }
}

fun <A, B> Assert<Either<A, B>>.isRightSatisfying(assertions: (B) -> Unit) = given { actual ->
    if (actual is Either.Right) {
        assertions(actual.value)
    } else {
        expected("$actual to be Either.Right")
    }
}

fun <A, B> Assert<Either<A, B>>.isLeftContaining(expected: A) =
    isLeftSatisfying { assertThat(it).isEqualTo(expected) }

fun <A, B> Assert<Either<A, B>>.isRightContaining(expected: B) =
    isRightSatisfying { assertThat(it).isEqualTo(expected) }
