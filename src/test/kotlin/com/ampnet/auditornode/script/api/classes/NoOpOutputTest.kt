package com.ampnet.auditornode.script.api.classes

import assertk.assertThat
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.script.api.ExecutionContext
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import com.ampnet.auditornode.script.api.objects.Properties
import com.ampnet.auditornode.service.impl.JavaScriptAuditingService
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class NoOpOutputTest : TestBase() {

    private val httpClient = HttpClient(mock())
    private val environment = Properties(mock())
    private val service = JavaScriptAuditingService(httpClient, environment)

    @Test
    fun `must not throw exception when renderText() is called`() {
        verify("call is successful") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    Output.renderText("test");
                    return AuditResult.success();
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp)
            assertThat(result).isRightContaining(SuccessfulAudit)
        }
    }

    @Test
    fun `must not throw exception when renderHtml() is called`() {
        verify("call is successful") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    Output.renderHtml("test");
                    return AuditResult.success();
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp)
            assertThat(result).isRightContaining(SuccessfulAudit)
        }
    }

    @Test
    fun `must not throw exception when renderMarkdown() is called`() {
        verify("call is successful") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    Output.renderMarkdown("test");
                    return AuditResult.success();
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp)
            assertThat(result).isRightContaining(SuccessfulAudit)
        }
    }
}
