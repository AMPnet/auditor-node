package com.ampnet.auditornode.script.api.classes

import assertk.assertThat
import assertk.assertions.isNotNull
import com.ampnet.auditornode.ApiTestBase
import com.ampnet.auditornode.UnitTestUtils.parseScriptId
import com.ampnet.auditornode.controller.websocket.WebSocketTestClient
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.model.websocket.AuditResultResponse
import com.ampnet.auditornode.model.websocket.ConnectedInfoMessage
import com.ampnet.auditornode.model.websocket.ExecutingInfoMessage
import com.ampnet.auditornode.model.websocket.ReadInputJsonCommand
import com.ampnet.auditornode.model.websocket.RenderHtmlCommand
import com.ampnet.auditornode.model.websocket.RenderMarkdownCommand
import com.ampnet.auditornode.model.websocket.RenderTextCommand
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
class WebSocketOutputApiTest : ApiTestBase() {

    @Inject
    @field:Client("/")
    private lateinit var webSocketClient: RxWebSocketClient

    @Test
    fun `must execute script which uses renderText() call`() {
        var storedScriptId: UUID? = null

        suppose("script is stored for interactive execution") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    Output.renderText("test");
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
            val client = webSocketClient.connect(WebSocketTestClient::class.java, "/script/interactive/$storedScriptId")
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(ReadInputJsonCommand())
            client.send("{}")
            client.assertNextMessage(ExecutingInfoMessage)
            client.assertNextMessage(RenderTextCommand("test"))
            client.assertNextMessage(AuditResultResponse(SuccessfulAudit, null))
            client.close()
        }
    }

    @Test
    fun `must execute script which uses renderHtml() call`() {
        var storedScriptId: UUID? = null

        suppose("script is stored for interactive execution") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    Output.renderHtml("test");
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
            val client = webSocketClient.connect(WebSocketTestClient::class.java, "/script/interactive/$storedScriptId")
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(ReadInputJsonCommand())
            client.send("{}")
            client.assertNextMessage(ExecutingInfoMessage)
            client.assertNextMessage(RenderHtmlCommand("test"))
            client.assertNextMessage(AuditResultResponse(SuccessfulAudit, null))
            client.close()
        }
    }

    @Test
    fun `must execute script which uses renderMarkdown() call`() {
        var storedScriptId: UUID? = null

        suppose("script is stored for interactive execution") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    Output.renderMarkdown("test");
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
            val client = webSocketClient.connect(WebSocketTestClient::class.java, "/script/interactive/$storedScriptId")
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(ReadInputJsonCommand())
            client.send("{}")
            client.assertNextMessage(ExecutingInfoMessage)
            client.assertNextMessage(RenderMarkdownCommand("test"))
            client.assertNextMessage(AuditResultResponse(SuccessfulAudit, null))
            client.close()
        }
    }
}
