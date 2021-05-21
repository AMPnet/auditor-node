package com.ampnet.auditornode.service

import assertk.assertThat
import assertk.assertions.isInstanceOf
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.isLeftContaining
import com.ampnet.auditornode.isLeftSatisfying
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.model.error.EvaluationError.InvalidReturnValueError
import com.ampnet.auditornode.model.error.EvaluationError.ScriptExecutionError
import com.ampnet.auditornode.script.api.ExecutionContext
import com.ampnet.auditornode.script.api.classes.HttpClient
import com.ampnet.auditornode.script.api.model.AbortedAudit
import com.ampnet.auditornode.script.api.model.FailedAudit
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import com.ampnet.auditornode.script.api.objects.Properties
import com.ampnet.auditornode.service.impl.JavaScriptAuditingService
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class JavaScriptAuditingServiceTest : TestBase() {

    private val httpClient = HttpClient(mock())
    private val environment = Properties(mock())
    private val service = JavaScriptAuditingService(httpClient, environment)

    @Test
    fun `must return ScriptExecutionError for invalid JavaScript source`() {
        verify("ScriptExecutionError is returned") {
            val scriptSource = "invalid script"
            val result = service.evaluate(scriptSource, ExecutionContext.noOp)
            assertThat(result).isLeftSatisfying {
                assertThat(it).isInstanceOf(ScriptExecutionError::class)
            }
        }
    }

    @Test
    fun `must return InvalidReturnValueError when JavaScript source returns native value`() {
        verify("InvalidReturnValueError is returned") {
            @Language("JavaScript") val scriptSource = """
                function audit() {
                    return { example: true };
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp)
            assertThat(result).isLeftContaining(InvalidReturnValueError("<native value>"))
        }
    }

    @Test
    fun `must return InvalidReturnValueError when JavaScript source returns unexpected JVM object`() {
        verify("InvalidReturnValueError is returned") {
            @Language("JavaScript") val scriptSource = """
                function audit() {
                    return AuditResult;
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp)
            assertThat(result).isLeftContaining(InvalidReturnValueError(Class::class.java))
        }
    }

    @Test
    fun `must return correct AuditResult from JavaScript source`() {
        verify("SuccessfulAudit is correctly returned") {
            @Language("JavaScript") val scriptSource = """
                function audit() {
                    return AuditResult.success();
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp)
            assertThat(result).isRightContaining(SuccessfulAudit)
        }

        verify("FailedAudit is correctly returned") {
            @Language("JavaScript") val scriptSource = """
                function audit() {
                    return AuditResult.failure("Example message");
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp)
            assertThat(result).isRightContaining(FailedAudit("Example message"))
        }

        verify("AbortedAudit is correctly returned") {
            @Language("JavaScript") val scriptSource = """
                function audit() {
                    return AuditResult.aborted("Example message");
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp)
            assertThat(result).isRightContaining(AbortedAudit("Example message"))
        }
    }
}
