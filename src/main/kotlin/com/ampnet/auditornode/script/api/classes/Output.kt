package com.ampnet.auditornode.script.api.classes

interface Output {
    fun renderText(text: String)
    fun renderHtml(html: String)
    fun renderMarkdown(markdown: String)
}
