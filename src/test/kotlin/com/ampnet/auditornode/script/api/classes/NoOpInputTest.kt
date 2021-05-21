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

class NoOpInputTest : TestBase() {

    private val httpClient = HttpClient(mock())
    private val environment = Properties(mock())
    private val service = JavaScriptAuditingService(httpClient, environment)

    @Test
    fun `must return null when readBoolean() is called`() {
        verify("null is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    assertNull("Input.readBoolean()", Input.readBoolean("test"));
                    return AuditResult.success();
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp)
            assertThat(result).isRightContaining(SuccessfulAudit)
        }
    }

    @Test
    fun `must return null when readNumber() is called`() {
        verify("null is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    assertNull("Input.readNumber()", Input.readNumber("test"));
                    return AuditResult.success();
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp)
            assertThat(result).isRightContaining(SuccessfulAudit)
        }
    }

    @Test
    fun `must return null when readString() is called`() {
        verify("null is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    assertNull("Input.readString()", Input.readString("test"));
                    return AuditResult.success();
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp)
            assertThat(result).isRightContaining(SuccessfulAudit)
        }
    }

    @Test
    fun `must return null when readFields() is called`() {
        verify("null is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    assertNull("Input.readFields()", Input.readFields({}, "test"));
                    return AuditResult.success();
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp)
            assertThat(result).isRightContaining(SuccessfulAudit)
        }
    }
}
