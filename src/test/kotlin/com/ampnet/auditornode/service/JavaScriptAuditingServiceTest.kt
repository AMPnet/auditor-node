package com.ampnet.auditornode.service

import assertk.assertThat
import assertk.assertions.isInstanceOf
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.TestUtils
import com.ampnet.auditornode.isLeftContaining
import com.ampnet.auditornode.isLeftSatisfying
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.model.error.EvaluationError.InvalidReturnValueError
import com.ampnet.auditornode.model.error.EvaluationError.ScriptExecutionError
import com.ampnet.auditornode.script.api.ExecutionContext
import com.ampnet.auditornode.script.api.classes.HttpClient
import com.ampnet.auditornode.script.api.model.AbortedAudit
import com.ampnet.auditornode.script.api.model.FailedAudit
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import com.ampnet.auditornode.script.api.objects.Properties
import com.ampnet.auditornode.service.impl.JavaScriptAuditingService
import com.fasterxml.jackson.databind.JsonNode
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
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
    fun `must return ScriptExecutionError for invalid Json input`() {
        verify("ScriptExecutionError is returned") {
            @Language("JavaScript") val scriptSource = """
                function audit(auditData) {
                    return AuditResult.success();
                }
            """.trimIndent()
            val inputJson = mock<JsonNode> {
                on { toString() } doReturn "invalid json"
            }
            val result = service.evaluate(scriptSource, ExecutionContext.noOp.copy(auditDataJson = inputJson))
            assertThat(result).isLeftSatisfying {
                assertThat(it).isInstanceOf(ScriptExecutionError::class)
            }
        }
    }

    @Test
    fun `must return InvalidReturnValueError when JavaScript source returns native value`() {
        verify("InvalidReturnValueError is returned") {
            @Language("JavaScript") val scriptSource = """
                function audit(auditData) {
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
                function audit(auditData) {
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
                function audit(auditData) {
                    return AuditResult.success();
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp)
            assertThat(result).isRightContaining(SuccessfulAudit)
        }

        verify("FailedAudit is correctly returned") {
            @Language("JavaScript") val scriptSource = """
                function audit(auditData) {
                    return AuditResult.failure("Example message");
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp)
            assertThat(result).isRightContaining(FailedAudit("Example message"))
        }

        verify("AbortedAudit is correctly returned") {
            @Language("JavaScript") val scriptSource = """
                function audit(auditData) {
                    return AuditResult.aborted("Example message");
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp)
            assertThat(result).isRightContaining(AbortedAudit("Example message"))
        }
    }

    @Test
    fun `must correctly read input JSON`() {
        verify("string input is correctly read") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    assertEquals("auditData", "input string", auditData);
                    return AuditResult.success();
                }
            """.trimIndent()
            val inputJson = TestUtils.objectMapper.nodeFactory.textNode("input string")
            val result = service.evaluate(scriptSource, ExecutionContext.noOp.copy(auditDataJson = inputJson))
            assertThat(result).isRightContaining(SuccessfulAudit)
        }

        verify("number input is correctly read") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    assertEquals("auditData", 123, auditData);
                    return AuditResult.success();
                }
            """.trimIndent()
            val inputJson = TestUtils.objectMapper.nodeFactory.numberNode(123)
            val result = service.evaluate(scriptSource, ExecutionContext.noOp.copy(auditDataJson = inputJson))
            assertThat(result).isRightContaining(SuccessfulAudit)
        }

        verify("boolean input is correctly read") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    assertEquals("auditData", true, auditData);
                    return AuditResult.success();
                }
            """.trimIndent()
            val inputJson = TestUtils.objectMapper.nodeFactory.booleanNode(true)
            val result = service.evaluate(scriptSource, ExecutionContext.noOp.copy(auditDataJson = inputJson))
            assertThat(result).isRightContaining(SuccessfulAudit)
        }

        verify("array input is correctly read") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    assertEquals("auditData.length", 3, auditData.length);
                    assertEquals("auditData[0]", 1, auditData[0]);
                    assertEquals("auditData[1]", 2, auditData[1]);
                    assertEquals("auditData[2]", 3, auditData[2]);
                    return AuditResult.success();
                }
            """.trimIndent()
            val inputJson = TestUtils.objectMapper.nodeFactory.arrayNode(3)
            inputJson.add(1)
            inputJson.add(2)
            inputJson.add(3)
            val result = service.evaluate(scriptSource, ExecutionContext.noOp.copy(auditDataJson = inputJson))
            assertThat(result).isRightContaining(SuccessfulAudit)
        }

        verify("object input is correctly read") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    assertEquals("auditData.testField1", "testValue", auditData.testField1);
                    assertEquals("auditData.testField2", true, auditData.testField2);
                    assertEquals("auditData.testField3", 123, auditData.testField3);
                    return AuditResult.success();
                }
            """.trimIndent()
            val inputJson = TestUtils.objectMapper.nodeFactory.objectNode()
            inputJson.put("testField1", "testValue")
            inputJson.put("testField2", true)
            inputJson.put("testField3", 123)
            val result = service.evaluate(scriptSource, ExecutionContext.noOp.copy(auditDataJson = inputJson))
            assertThat(result).isRightContaining(SuccessfulAudit)
        }
    }
}
