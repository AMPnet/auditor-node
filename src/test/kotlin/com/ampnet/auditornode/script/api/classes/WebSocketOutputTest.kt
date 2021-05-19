package com.ampnet.auditornode.script.api.classes

import assertk.assertThat
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.model.websocket.RenderHtmlCommand
import com.ampnet.auditornode.model.websocket.RenderMarkdownCommand
import com.ampnet.auditornode.model.websocket.RenderTextCommand
import com.ampnet.auditornode.model.websocket.WebSocketApi
import com.ampnet.auditornode.model.websocket.WebSocketMessage
import com.ampnet.auditornode.script.api.ExecutionContext
import com.ampnet.auditornode.script.api.model.AuditResult
import com.ampnet.auditornode.script.api.objects.Properties
import com.ampnet.auditornode.service.impl.JavaScriptAuditingService
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.jackson.serialize.JacksonObjectSerializer
import io.micronaut.websocket.WebSocketSession
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.then
import org.mockito.kotlin.times
import java.nio.charset.StandardCharsets

class WebSocketOutputTest : TestBase() {

    private val session = mock<WebSocketSession>()
    private val objectSerializer = JacksonObjectSerializer(ObjectMapper())
    private val webSocketApi = WebSocketApi(session, objectSerializer)
    private val httpClient = HttpClient(mock())
    private val environment = Properties(mock())
    private val service = JavaScriptAuditingService(httpClient, environment)

    private fun WebSocketMessage.toJson(): String {
        return objectSerializer.serialize(this)
            .map { String(it, StandardCharsets.UTF_8) }
            .orElse("")
    }

    @BeforeEach
    fun beforeEach() {
        reset(session)
    }

    @Test
    fun `must correctly send render text command`() {
        val output = WebSocketOutput(webSocketApi)

        suppose("renderText() output is called") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    Output.renderText("some text");
                    return AuditResult.of(true);
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp.copy(output = output))
            assertThat(result).isRightContaining(AuditResult(true))
        }

        verify("correct web socket command is sent") {
            then(session)
                .should(times(1))
                .sendSync(RenderTextCommand("some text").toJson())
        }
    }

    @Test
    fun `must correctly send render html command`() {
        val output = WebSocketOutput(webSocketApi)

        suppose("renderHtml() output is called") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    Output.renderHtml("some html");
                    return AuditResult.of(true);
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp.copy(output = output))
            assertThat(result).isRightContaining(AuditResult(true))
        }

        verify("correct web socket command is sent") {
            then(session)
                .should(times(1))
                .sendSync(RenderHtmlCommand("some html").toJson())
        }
    }

    @Test
    fun `must correctly send render markdown command`() {
        val output = WebSocketOutput(webSocketApi)

        suppose("renderMarkdown() output is called") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    Output.renderMarkdown("some markdown");
                    return AuditResult.of(true);
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp.copy(output = output))
            assertThat(result).isRightContaining(AuditResult(true))
        }

        verify("correct web socket command is sent") {
            then(session)
                .should(times(1))
                .sendSync(RenderMarkdownCommand("some markdown").toJson())
        }
    }
}
