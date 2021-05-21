package com.ampnet.auditornode.script.api.classes

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.ampnet.auditornode.ApiTestBase
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

@MicronautTest
class NoOpIpfsApiTest : ApiTestBase() {

    @Test
    fun `must execute script which uses getFile() call`() {
        verify("null is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    assertNull("Ipfs.getFile()", Ipfs.getFile("example.js"));
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
}
