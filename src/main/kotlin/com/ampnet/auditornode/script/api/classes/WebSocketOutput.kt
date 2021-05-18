package com.ampnet.auditornode.script.api.classes

import io.micronaut.websocket.WebSocketSession
import org.graalvm.polyglot.HostAccess.Export

class WebSocketOutput(private val session: WebSocketSession) : Output { // TODO refactor WebSocket commands
// TODO test

    @Export
    override fun renderText(text: String) {
        session.sendSync("renderText:$text")
    }

    @Export
    override fun renderHtml(html: String) {
        session.sendSync("renderHtml:$html")
    }

    @Export
    override fun renderMarkdown(markdown: String) {
        session.sendSync("renderMarkdown:$markdown")
    }
}
