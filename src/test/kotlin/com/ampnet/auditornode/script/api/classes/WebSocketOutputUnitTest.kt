package com.ampnet.auditornode.script.api.classes

import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.model.websocket.RenderHtmlCommand
import com.ampnet.auditornode.model.websocket.RenderMarkdownCommand
import com.ampnet.auditornode.model.websocket.RenderTextCommand
import com.ampnet.auditornode.model.websocket.WebSocketApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.then
import org.mockito.kotlin.times

class WebSocketOutputUnitTest : TestBase() {

    private val webSocketApi = mock<WebSocketApi>()
    private val service = WebSocketOutput(webSocketApi)

    @BeforeEach
    fun beforeEach() {
        reset(webSocketApi)
    }

    @Test
    fun `must correctly send web socket command for renderText() call`() {
        val text = "test text"

        suppose("renderText() is called") {
            service.renderText(text)
        }

        verify("correct web socket command is sent") {
            then(webSocketApi)
                .should(times(1))
                .sendCommand(RenderTextCommand(text))
        }
    }

    @Test
    fun `must correctly send web socket command for renderHtml() call`() {
        val html = "test html"

        suppose("renderHtml() is called") {
            service.renderHtml(html)
        }

        verify("correct web socket command is sent") {
            then(webSocketApi)
                .should(times(1))
                .sendCommand(RenderHtmlCommand(html))
        }
    }

    @Test
    fun `must correctly send web socket command for renderMarkdown() call`() {
        val markdown = "test markdown"

        suppose("renderMarkdown() is called") {
            service.renderMarkdown(markdown)
        }

        verify("correct web socket command is sent") {
            then(webSocketApi)
                .should(times(1))
                .sendCommand(RenderMarkdownCommand(markdown))
        }
    }
}
