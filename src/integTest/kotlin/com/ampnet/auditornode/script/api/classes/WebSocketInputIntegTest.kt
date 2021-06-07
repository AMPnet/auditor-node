package com.ampnet.auditornode.script.api.classes

import assertk.assertThat
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.TestUtils
import com.ampnet.auditornode.TestUtils.toJson
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.model.websocket.ButtonCommand
import com.ampnet.auditornode.model.websocket.InputField
import com.ampnet.auditornode.model.websocket.InputType
import com.ampnet.auditornode.model.websocket.ReadBooleanCommand
import com.ampnet.auditornode.model.websocket.ReadFieldsCommand
import com.ampnet.auditornode.model.websocket.ReadNumberCommand
import com.ampnet.auditornode.model.websocket.ReadStringCommand
import com.ampnet.auditornode.model.websocket.WebSocketApi
import com.ampnet.auditornode.script.api.ExecutionContext
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import com.ampnet.auditornode.script.api.objects.Properties
import com.ampnet.auditornode.service.impl.JavaScriptAuditingService
import io.micronaut.websocket.WebSocketSession
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.then
import org.mockito.kotlin.times

class WebSocketInputIntegTest : TestBase() {

    private val session = mock<WebSocketSession>()
    private val webSocketApi = WebSocketApi(session, TestUtils.objectMapper)
    private val httpClient = HttpClient(mock())
    private val environment = Properties(mock())
    private val service = JavaScriptAuditingService(httpClient, environment)

    @BeforeEach
    fun beforeEach() {
        reset(session)
    }

    @Test
    fun `must correctly read boolean from web socket`() {
        val input = WebSocketInput(webSocketApi)

        suppose("some boolean values will be sent via web socket") {
            input.push("true")
            input.push("false")
            input.push("invalid boolean")
        }

        verify("boolean values are correctly read from web socket") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    assertEquals("Input.readBoolean() #1", true, Input.readBoolean("test1"));
                    assertEquals("Input.readBoolean() #2", false, Input.readBoolean("test2"));
                    assertEquals("Input.readBoolean() #3", false, Input.readBoolean("test3"));
                    return AuditResult.success();
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp.copy(input = input))
            assertThat(result).isRightContaining(SuccessfulAudit)
        }

        verify("correct web socket commands are sent") {
            then(session)
                .should(times(1))
                .sendSync(ReadBooleanCommand("test1").toJson())
            then(session)
                .should(times(1))
                .sendSync(ReadBooleanCommand("test2").toJson())
            then(session)
                .should(times(1))
                .sendSync(ReadBooleanCommand("test3").toJson())
        }
    }

    @Test
    fun `must correctly read number from web socket`() {
        val input = WebSocketInput(webSocketApi)

        suppose("some number values will be sent via web socket") {
            input.push("1")
            input.push("-2.0")
            input.push("invalid number")
        }

        verify("number values are correctly read from web socket") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    assertEquals("Input.readNumber() #1", 1, Input.readNumber("test1"));
                    assertEquals("Input.readNumber() #2", -2.0, Input.readNumber("test2"));
                    assertNull("Input.readNumber() #3", Input.readNumber("test3"));
                    return AuditResult.success();
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp.copy(input = input))
            assertThat(result).isRightContaining(SuccessfulAudit)
        }

        verify("correct web socket commands are sent") {
            then(session)
                .should(times(1))
                .sendSync(ReadNumberCommand("test1").toJson())
            then(session)
                .should(times(1))
                .sendSync(ReadNumberCommand("test2").toJson())
            then(session)
                .should(times(1))
                .sendSync(ReadNumberCommand("test3").toJson())
        }
    }

    @Test
    fun `must correctly read string from web socket`() {
        val input = WebSocketInput(webSocketApi)

        suppose("some string values will be sent via web socket") {
            input.push("string1")
            input.push("string2")
        }

        verify("string values are correctly read from web socket") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    assertEquals("Input.readString() #1", "string1", Input.readString("test1"));
                    assertEquals("Input.readString() #2", "string2", Input.readString("test2"));
                    return AuditResult.success();
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp.copy(input = input))
            assertThat(result).isRightContaining(SuccessfulAudit)
        }

        verify("correct web socket commands are sent") {
            then(session)
                .should(times(1))
                .sendSync(ReadStringCommand("test1").toJson())
            then(session)
                .should(times(1))
                .sendSync(ReadStringCommand("test2").toJson())
        }
    }

    @Test
    fun `must correctly read fields from web socket`() {
        val input = WebSocketInput(webSocketApi)

        suppose("some values will be sent via web socket") {
            input.push("true")
            input.push("42")
            input.push("string field")
        }

        verify("field values are correctly read from web socket") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    const fields = [
                        {
                            "type": "boolean",
                            "name": "booleanField",
                            "description": "test1"
                        },
                        {
                            "type": "number",
                            "name": "numberField",
                            "description": "test2"
                        },
                        {
                            "type": "string",
                            "name": "stringField",
                            "description": "test3"
                        }
                    ];
                    const values = Input.readFields(fields, "test");

                    assertEquals("values.get(\"booleanField\")", true, values.get("booleanField"));
                    assertEquals("values.get(\"numberField\")", 42, values.get("numberField"));
                    assertEquals("values.get(\"stringField\")", "string field", values.get("stringField"));
                    return AuditResult.success();
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp.copy(input = input))
            assertThat(result).isRightContaining(SuccessfulAudit)
        }

        verify("correct web socket commands are sent") {
            then(session)
                .should(times(1))
                .sendSync(
                    ReadFieldsCommand(
                        "test",
                        listOf(
                            InputField(
                                type = InputType.BOOLEAN,
                                name = "booleanField",
                                description = "test1"
                            ),
                            InputField(
                                type = InputType.NUMBER,
                                name = "numberField",
                                description = "test2"
                            ),
                            InputField(
                                type = InputType.STRING,
                                name = "stringField",
                                description = "test3"
                            )
                        )
                    ).toJson()
                )
        }
    }

    @Test
    fun `must correctly read button click from web socket`() {
        val input = WebSocketInput(webSocketApi)

        suppose("some value will be sent via web socket") {
            input.push("ignored dummy value")
        }

        verify("button click is correctly read from web socket") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    Input.button("test");
                    return AuditResult.success();
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp.copy(input = input))
            assertThat(result).isRightContaining(SuccessfulAudit)
        }

        verify("correct web socket commands are sent") {
            then(session)
                .should(times(1))
                .sendSync(ButtonCommand("test").toJson())
        }
    }
}
