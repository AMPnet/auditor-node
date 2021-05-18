package com.ampnet.auditornode.script.api.classes

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.ampnet.auditornode.ApiTestBase
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.script.api.model.AuditResult
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

@MicronautTest
class NoOpInputApiTest : ApiTestBase() {

    @Test
    fun `must execute script which uses readBoolean() call`() {
        verify("null is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    assertNull("Input.readBoolean()", Input.readBoolean("test"));
                    return AuditResult.of(true);
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/execute", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            assertThat(result).isEqualTo(AuditResult(true).right().toString())
        }
    }

    @Test
    fun `must execute script which uses readNumber() call`() {
        verify("null is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    assertNull("Input.readNumber()", Input.readNumber("test"));
                    return AuditResult.of(true);
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/execute", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            assertThat(result).isEqualTo(AuditResult(true).right().toString())
        }
    }

    @Test
    fun `must execute script which uses readString() call`() {
        verify("null is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    assertNull("Input.readString()", Input.readString("test"));
                    return AuditResult.of(true);
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/execute", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            assertThat(result).isEqualTo(AuditResult(true).right().toString())
        }
    }

    @Test
    fun `must execute script which uses readFields() call`() {
        verify("null is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    assertNull("Input.readFields()", Input.readFields({}, "test"));
                    return AuditResult.of(true);
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/execute", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            assertThat(result).isEqualTo(AuditResult(true).right().toString())
        }
    }
}
