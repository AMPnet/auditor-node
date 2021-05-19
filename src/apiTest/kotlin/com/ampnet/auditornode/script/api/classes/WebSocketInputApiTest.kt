package com.ampnet.auditornode.script.api.classes

import assertk.assertThat
import assertk.assertions.isNotNull
import com.ampnet.auditornode.ApiTestBase
import com.ampnet.auditornode.controller.websocket.WebSocketTestClient
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.model.websocket.AuditResultResponse
import com.ampnet.auditornode.model.websocket.ConnectedInfoMessage
import com.ampnet.auditornode.model.websocket.ExecutingInfoMessage
import com.ampnet.auditornode.model.websocket.InputField
import com.ampnet.auditornode.model.websocket.InputType
import com.ampnet.auditornode.model.websocket.ReadBooleanCommand
import com.ampnet.auditornode.model.websocket.ReadFieldsCommand
import com.ampnet.auditornode.model.websocket.ReadNumberCommand
import com.ampnet.auditornode.model.websocket.ReadStringCommand
import com.ampnet.auditornode.script.api.model.AuditResult
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.websocket.RxWebSocketClient
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import javax.inject.Inject

@MicronautTest
class WebSocketInputApiTest : ApiTestBase() {

    companion object {
        @Language("RegExp")
        private const val UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
    }

    @Inject
    @field:Client("/")
    private lateinit var webSocketClient: RxWebSocketClient

    @Test
    fun `must execute script which uses readBoolean() call`() {
        var storedScriptId: String? = null

        suppose("script is stored for interactive execution") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    assertEquals("Input.readBoolean()", true, Input.readBoolean("test"));
                    return AuditResult.of(true);
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/store", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            val responseRegex = """^\{"id":"($UUID_REGEX)"}$""".toRegex()
            val matchResult = responseRegex.find(result)
                ?: fail("Response does not match regular expression: $responseRegex")

            storedScriptId = matchResult.groups[1]?.value
            assertThat(storedScriptId).isNotNull()
        }

        verify("correct value is returned") {
            val client = webSocketClient.connect(WebSocketTestClient::class.java, "/script/interactive/$storedScriptId")
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(ExecutingInfoMessage)
            client.assertNextMessage(ReadBooleanCommand("test"))
            client.send("true")
            client.assertNextMessage(
                AuditResultResponse(
                    message = "Script execution finished",
                    payload = AuditResult(true)
                )
            )
            client.close()
        }
    }

    @Test
    fun `must execute script which uses readNumber() call`() {
        var storedScriptId: String? = null

        suppose("script is stored for interactive execution") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    assertEquals("Input.readNumber()", 123, Input.readNumber("test"));
                    return AuditResult.of(true);
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/store", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            val responseRegex = """^\{"id":"($UUID_REGEX)"}$""".toRegex()
            val matchResult = responseRegex.find(result)
                ?: fail("Response does not match regular expression: $responseRegex")

            storedScriptId = matchResult.groups[1]?.value
            assertThat(storedScriptId).isNotNull()
        }

        verify("correct value is returned") {
            val client = webSocketClient.connect(WebSocketTestClient::class.java, "/script/interactive/$storedScriptId")
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(ExecutingInfoMessage)
            client.assertNextMessage(ReadNumberCommand("test"))
            client.send("123")
            client.assertNextMessage(
                AuditResultResponse(
                    message = "Script execution finished",
                    payload = AuditResult(true)
                )
            )
            client.close()
        }
    }

    @Test
    fun `must execute script which uses readString() call`() {
        var storedScriptId: String? = null

        suppose("script is stored for interactive execution") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    assertEquals("Input.readString()", "example", Input.readString("test"));
                    return AuditResult.of(true);
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/store", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            val responseRegex = """^\{"id":"($UUID_REGEX)"}$""".toRegex()
            val matchResult = responseRegex.find(result)
                ?: fail("Response does not match regular expression: $responseRegex")

            storedScriptId = matchResult.groups[1]?.value
            assertThat(storedScriptId).isNotNull()
        }

        verify("correct value is returned") {
            val client = webSocketClient.connect(WebSocketTestClient::class.java, "/script/interactive/$storedScriptId")
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(ExecutingInfoMessage)
            client.assertNextMessage(ReadStringCommand("test"))
            client.send("example")
            client.assertNextMessage(
                AuditResultResponse(
                    message = "Script execution finished",
                    payload = AuditResult(true)
                )
            )
            client.close()
        }
    }

    @Test
    fun `must execute script which uses readFields() call`() {
        var storedScriptId: String? = null

        suppose("script is stored for interactive execution") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    let fields = [
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
                    let values = Input.readFields(fields, "test");

                    assertEquals("values.get(\"booleanField\")", true, values.get("booleanField"));
                    assertEquals("values.get(\"numberField\")", 42, values.get("numberField"));
                    assertEquals("values.get(\"stringField\")", "string field", values.get("stringField"));
                    return AuditResult.of(true);
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/store", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            val responseRegex = """^\{"id":"($UUID_REGEX)"}$""".toRegex()
            val matchResult = responseRegex.find(result)
                ?: fail("Response does not match regular expression: $responseRegex")

            storedScriptId = matchResult.groups[1]?.value
            assertThat(storedScriptId).isNotNull()
        }

        verify("correct values are returned") {
            val client = webSocketClient.connect(WebSocketTestClient::class.java, "/script/interactive/$storedScriptId")
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(ExecutingInfoMessage)

            val fields = listOf(
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

            client.assertNextMessage(ReadFieldsCommand("test", fields))
            client.send("true")
            client.send("42")
            client.send("string field")
            client.assertNextMessage(
                AuditResultResponse(
                    message = "Script execution finished",
                    payload = AuditResult(true)
                )
            )
            client.close()
        }
    }
}
