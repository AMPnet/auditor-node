package com.ampnet.auditornode.script.api.classes

import com.ampnet.auditornode.model.websocket.RenderHtmlCommand
import com.ampnet.auditornode.model.websocket.RenderMarkdownCommand
import com.ampnet.auditornode.model.websocket.RenderTextCommand
import com.ampnet.auditornode.model.websocket.WebSocketApi
import org.graalvm.polyglot.HostAccess.Export

class WebSocketOutput(private val webSocketApi: WebSocketApi) : Output { // TODO test

    @Export
    override fun renderText(text: String) {
        webSocketApi.sendCommand(RenderTextCommand(text))
    }

    @Export
    override fun renderHtml(html: String) {
        webSocketApi.sendCommand(RenderHtmlCommand(html))
    }

    @Export
    override fun renderMarkdown(markdown: String) {
        webSocketApi.sendCommand(RenderMarkdownCommand(markdown))
    }
}
