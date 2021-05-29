package com.ampnet.auditornode.controller.websocket

import assertk.assertThat
import assertk.assertions.isNotNull
import com.ampnet.auditornode.ApiTestBase
import com.ampnet.auditornode.TestUtils.parseScriptId
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.model.websocket.AuditResultResponse
import com.ampnet.auditornode.model.websocket.ConnectedInfoMessage
import com.ampnet.auditornode.model.websocket.ErrorResponse
import com.ampnet.auditornode.model.websocket.ExecutingInfoMessage
import com.ampnet.auditornode.model.websocket.InvalidInputJsonInfoMessage
import com.ampnet.auditornode.model.websocket.NotFoundInfoMessage
import com.ampnet.auditornode.model.websocket.ReadInputJsonCommand
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.websocket.RxWebSocketClient
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import java.util.UUID
import javax.inject.Inject

@MicronautTest
class InteractiveScriptWebSocketApiTest : ApiTestBase() {

    @Inject
    @field:Client("/")
    private lateinit var webSocketClient: RxWebSocketClient

    @Test
    fun `must return not found message for non-existent script`() {
        verify("correct web socket message is received") {
            val client = webSocketClient
                .connect(WebSocketTestClient::class.java, "/script/interactive/${UUID.randomUUID()}")
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(NotFoundInfoMessage)
            client.close()
        }
    }

    @Test
    fun `must return audit result message for successful script`() {
        var storedScriptId: UUID? = null

        suppose("script is stored for interactive execution") {
            @Language("JavaScript") val scriptSource = """
                function audit(auditData) {
                    return AuditResult.success();
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/store", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            storedScriptId = result.parseScriptId()
            assertThat(storedScriptId).isNotNull()
        }

        verify("correct web socket message is received") {
            val client = webSocketClient
                .connect(WebSocketTestClient::class.java, "/script/interactive/$storedScriptId")
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(ReadInputJsonCommand())
            client.send("{}")
            client.assertNextMessage(ExecutingInfoMessage)
            client.assertNextMessage(AuditResultResponse(SuccessfulAudit))
            client.close()
        }
    }

    @Test
    fun `must correctly read input JSON and return audit result message for successful script`() {
        var storedScriptId: UUID? = null

        suppose("script is stored for interactive execution") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    assertEquals("auditData.exampleValue", 123, auditData.exampleValue);
                    return AuditResult.success();
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/store", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            storedScriptId = result.parseScriptId()
            assertThat(storedScriptId).isNotNull()
        }

        verify("correct web socket message is received") {
            val client = webSocketClient
                .connect(WebSocketTestClient::class.java, "/script/interactive/$storedScriptId")
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(ReadInputJsonCommand())
            client.send("{\"exampleValue\":123}")
            client.assertNextMessage(ExecutingInfoMessage)
            client.assertNextMessage(AuditResultResponse(SuccessfulAudit))
            client.close()
        }
    }

    @Test
    fun `must return error result message for invalid script`() {
        var storedScriptId: UUID? = null

        suppose("script is stored for interactive execution") {
            @Language("JavaScript") val scriptSource = """
                function audit(auditData) {
                    throw "error";
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/store", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            storedScriptId = result.parseScriptId()
            assertThat(storedScriptId).isNotNull()
        }

        verify("correct web socket message is received") {
            val client = webSocketClient
                .connect(WebSocketTestClient::class.java, "/script/interactive/$storedScriptId")
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(ReadInputJsonCommand())
            client.send("{}")
            client.assertNextMessage(ExecutingInfoMessage)
            client.assertNextMessage(ErrorResponse("Error while executing provided script: error"))
            client.close()
        }
    }

    @Test
    fun `must return error result message for invalid script input JSON`() {
        var storedScriptId: UUID? = null

        suppose("script is stored for interactive execution") {
            @Language("JavaScript") val scriptSource = """
                function audit(auditData) {
                    return AuditResult.success();
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/store", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            storedScriptId = result.parseScriptId()
            assertThat(storedScriptId).isNotNull()
        }

        verify("correct web socket message is received") {
            val client = webSocketClient
                .connect(WebSocketTestClient::class.java, "/script/interactive/$storedScriptId")
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(ReadInputJsonCommand())
            client.send("invalid json")
            client.assertNextMessage(InvalidInputJsonInfoMessage)
            client.close()
        }
    }
}
