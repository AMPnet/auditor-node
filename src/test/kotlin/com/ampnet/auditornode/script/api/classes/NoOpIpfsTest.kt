package com.ampnet.auditornode.script.api.classes

import assertk.assertThat
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.script.api.ExecutionContext
import com.ampnet.auditornode.script.api.model.AuditResult
import com.ampnet.auditornode.script.api.objects.Properties
import com.ampnet.auditornode.service.impl.JavaScriptAuditingService
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class NoOpIpfsTest : TestBase() {

    private val httpClient = HttpClient(mock())
    private val environment = Properties(mock())
    private val service = JavaScriptAuditingService(httpClient, environment)

    @Test
    fun `must return null when getFile() is called`() {
        verify("call is successful") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    assertNull("Ipfs.getFile()", Ipfs.getFile("test"));
                    return AuditResult.of(true);
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp)
            assertThat(result).isRightContaining(AuditResult(true))
        }
    }

}
