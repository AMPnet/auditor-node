package com.ampnet.auditornode.script.api.classes

import assertk.assertThat
import assertk.assertions.isNotNull
import com.ampnet.auditornode.ApiTestBase
import com.ampnet.auditornode.controller.websocket.WebSocketTestClient
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.model.websocket.AuditResultResponse
import com.ampnet.auditornode.model.websocket.ConnectedInfoMessage
import com.ampnet.auditornode.model.websocket.ExecutingInfoMessage
import com.ampnet.auditornode.model.websocket.RenderHtmlCommand
import com.ampnet.auditornode.model.websocket.RenderMarkdownCommand
import com.ampnet.auditornode.model.websocket.RenderTextCommand
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
class WebSocketOutputApiTest : ApiTestBase() {

    companion object {
        @Language("RegExp")
        private const val UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
    }

    @Inject
    @field:Client("/")
    private lateinit var webSocketClient: RxWebSocketClient

    @Test
    fun `must execute script which uses renderText() call`() {
        var storedScriptId: String? = null

        suppose("script is stored for interactive execution") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    Output.renderText("test");
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

        verify("correct web socket message is received") {
            val client = webSocketClient.connect(WebSocketTestClient::class.java, "/script/interactive/$storedScriptId")
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(ExecutingInfoMessage)
            client.assertNextMessage(RenderTextCommand("test"))
            client.assertNextMessage(AuditResultResponse(AuditResult(true)))
            client.close()
        }
    }

    @Test
    fun `must execute script which uses renderHtml() call`() {
        var storedScriptId: String? = null

        suppose("script is stored for interactive execution") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    Output.renderHtml("test");
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

        verify("correct web socket message is received") {
            val client = webSocketClient.connect(WebSocketTestClient::class.java, "/script/interactive/$storedScriptId")
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(ExecutingInfoMessage)
            client.assertNextMessage(RenderHtmlCommand("test"))
            client.assertNextMessage(AuditResultResponse(AuditResult(true)))
            client.close()
        }
    }

    @Test
    fun `must execute script which uses renderMarkdown() call`() {
        var storedScriptId: String? = null

        suppose("script is stored for interactive execution") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    Output.renderMarkdown("test");
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

        verify("correct web socket message is received") {
            val client = webSocketClient.connect(WebSocketTestClient::class.java, "/script/interactive/$storedScriptId")
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(ExecutingInfoMessage)
            client.assertNextMessage(RenderMarkdownCommand("test"))
            client.assertNextMessage(AuditResultResponse(AuditResult(true)))
            client.close()
        }
    }
}
