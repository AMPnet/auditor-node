package com.ampnet.auditornode.script.api.classes

import org.graalvm.polyglot.HostAccess.Export

object NoOpOutput : Output {

    @Export
    override fun renderText(text: String) = Unit

    @Export
    override fun renderHtml(html: String) = Unit

    @Export
    override fun renderMarkdown(markdown: String) = Unit
}
