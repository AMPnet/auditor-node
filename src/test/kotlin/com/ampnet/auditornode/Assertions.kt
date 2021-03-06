package com.ampnet.auditornode

import arrow.core.Either
import assertk.Assert
import assertk.assertions.isEqualTo
import assertk.assertions.support.expected
import assertk.assertions.support.fail
import com.fasterxml.jackson.databind.JsonNode
import org.intellij.lang.annotations.Language

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

fun Assert<String>.isJsonEqualTo(expected: Any) = given { actual ->
    val actualJson = UnitTestUtils.objectMapper.readTree(actual)
    val expectedJson = UnitTestUtils.objectMapper.valueToTree<JsonNode>(expected)

    if (actualJson != expectedJson) {
        fail(expectedJson, actualJson)
    }
}

@Language("JavaScript")
val jsAssertions = """
    function assertEquals(name, expected, actual) {
        if (expected !== actual) {
            const message = "Assertion failed for '" + name + "'; expected: '" + expected + "', actual: '" + actual +
                "'";
            console.log(message);
            throw message;
        } else {
            console.log("Assertion success for '" + name + "', got value: '" + actual + "'");
        }
    }

    function assertThrows(name, expected, code) {
        try {
            code();
        } catch (e) {
            assertEquals("exception thrown by " + name, expected, `${'$'}{e}`);
            return;
        }

        const message = "Expected '" + name + "' to throw an exception";
        console.log(message);
        throw message;
    }

    function assertNonNull(name, value) {
        if (value === null || value === undefined) {
            const message = "Assertion failed for '" + name + "'; expected non-null (defined) value";
            console.log(message);
            throw message;
        } else {
            console.log("Assertion success for '" + name + "', got value: '" + value + "'");
        }
    }

    function assertNull(name, value) {
        if (value === null) {
            console.log("Assertion success for '" + name + "', got null as expected");
        } else {
            const message = "Assertion failed for '" + name + "'; expected null, got value: '" + value + "'";
            console.log(message);
            throw message;
        }
    }
""".trimIndent().plus("\n\n")
