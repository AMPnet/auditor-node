package com.ampnet.auditornode.script.api.classes

import com.amptnet.auditornode.documentation.annotation.ScriptApi
import com.amptnet.auditornode.documentation.annotation.ScriptApiCategory
import com.amptnet.auditornode.documentation.annotation.ScriptFunction

@ScriptApi(
    description = "Provides support for rendering text, HTML and Markdown when the script is running interactively " +
        "via web socket. When the script is not running interactively, the methods will do nothing. See web socket " +
        "documentation for more info on running scripts interactively.",
    category = ScriptApiCategory.API,
    hasStaticApi = true
)
interface Output {

    @ScriptFunction(
        description = "Requests rendering of provided text.",
        exampleCall = "`{apiObjectName}.renderText(\"example\");`"
    )
    fun renderText(text: String)

    @ScriptFunction(
        description = "Requests rendering of provided HTML.",
        exampleCall = "`{apiObjectName}.renderHtml(\"<p>example<p/>\");`"
    )
    fun renderHtml(html: String)

    @ScriptFunction(
        description = "Requests rendering of provided Markdown.",
        exampleCall = "`{apiObjectName}.renderMarkdown(\"# Example\");`"
    )
    fun renderMarkdown(markdown: String)
}
