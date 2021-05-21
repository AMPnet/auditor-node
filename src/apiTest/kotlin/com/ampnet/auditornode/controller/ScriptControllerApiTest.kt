package com.ampnet.auditornode.controller

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.startsWith
import com.ampnet.auditornode.ApiTestBase
import com.ampnet.auditornode.TestUtils.parseScriptId
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import java.util.UUID

@MicronautTest
class ScriptControllerApiTest : ApiTestBase() {

    @Test
    fun `must correctly execute simple auditing script`() {
        verify("simple auditing script is correctly executed") {
            @Language("JavaScript") val scriptSource = """
                function audit() {
                    return AuditResult.success();
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/execute", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            assertThat(result).isEqualTo(SuccessfulAudit.right().toString())
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

    @Test
    fun `must correctly store and load scripts`() {
        val scriptSource = "test script source"
        var storedScriptId: UUID? = null

        verify("script is stored and script ID is returned") {
            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/store", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            storedScriptId = result.parseScriptId()
            assertThat(storedScriptId).isNotNull()
        }

        verify("stored script is correctly loaded") {
            val result = client.toBlocking().retrieve(HttpRequest.GET<String>("/script/load/$storedScriptId"))
            assertThat(result).isEqualTo(scriptSource)
        }
    }

    @Test
    fun `must return 404 status code when fetching non-existent script`() {
        verify("404 status code is returned when fetching non-existent script") {
            assertThat {
                client.toBlocking().exchange(
                    HttpRequest.GET<String>("/script/load/${UUID.randomUUID()}"),
                    String::class.java
                )
            }
                .isFailure()
                .isInstanceOf(HttpClientResponseException::class)
                .given {
                    assertThat(it.status).isEqualTo(HttpStatus.NOT_FOUND)
                }
        }
    }
}
