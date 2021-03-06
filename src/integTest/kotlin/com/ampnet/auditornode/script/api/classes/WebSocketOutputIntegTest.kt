package com.ampnet.auditornode.script.api.classes

import assertk.assertThat
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.UnitTestUtils
import com.ampnet.auditornode.UnitTestUtils.toJson
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.model.websocket.RenderHtmlCommand
import com.ampnet.auditornode.model.websocket.RenderMarkdownCommand
import com.ampnet.auditornode.model.websocket.RenderTextCommand
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

class WebSocketOutputIntegTest : TestBase() {

    private val session = mock<WebSocketSession>()
    private val webSocketApi = WebSocketApi(session, UnitTestUtils.objectMapper)
    private val httpClient = HttpClient(mock())
    private val environment = Properties(mock())
    private val service = JavaScriptAuditingService(httpClient, environment)

    @BeforeEach
    fun beforeEach() {
        reset(session)
    }

    @Test
    fun `must correctly send render text command`() {
        val output = WebSocketOutput(webSocketApi)

        suppose("renderText() output is called") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    Output.renderText("some text");
                    return AuditResult.success();
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp.copy(output = output))
            assertThat(result).isRightContaining(SuccessfulAudit)
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
                function audit(auditData) {
                    Output.renderHtml("some html");
                    return AuditResult.success();
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp.copy(output = output))
            assertThat(result).isRightContaining(SuccessfulAudit)
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
                function audit(auditData) {
                    Output.renderMarkdown("some markdown");
                    return AuditResult.success();
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource, ExecutionContext.noOp.copy(output = output))
            assertThat(result).isRightContaining(SuccessfulAudit)
        }

        verify("correct web socket command is sent") {
            then(session)
                .should(times(1))
                .sendSync(RenderMarkdownCommand("some markdown").toJson())
        }
    }
}
