package com.ampnet.auditornode.controller

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.startsWith
import com.ampnet.auditornode.ApiTestBase
import com.ampnet.auditornode.script.api.model.AuditResult
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

@MicronautTest
class ScriptControllerApiTest : ApiTestBase() {

    @Test
    fun `must correctly execute simple auditing script`() {
        verify("simple auditing script is correctly executed") {
            @Language("JavaScript") val scriptSource = """
                function audit() {
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
    fun `must return error when executing invalid auditing script`() {
        verify("error is returned for incorrect auditing script") {
            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/execute", "invalid script").apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            assertThat(result).startsWith("Either.Left")
        }
    }
}
