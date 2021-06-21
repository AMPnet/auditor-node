package com.ampnet.auditornode.script.api.classes

import com.ampnet.auditornode.script.api.model.MapApi
import com.amptnet.auditornode.documentation.annotation.ScriptApi
import com.amptnet.auditornode.documentation.annotation.ScriptApiCategory
import com.amptnet.auditornode.documentation.annotation.ScriptFunction
import org.graalvm.polyglot.Value

@ScriptApi(
    description = "Provides support for user input when script is running interactively via web socket. When script " +
        "is not running interactively, all methods of this object will always return `null`. See web socket " +
        "documentation for more info on running scripts interactively.",
    category = ScriptApiCategory.API,
    hasStaticApi = true,
    additionalFunctionsDocumentation = ["input-fields-description.md"]
)
interface Input {

    @ScriptFunction(
        description = "Requests a boolean input from the user via web socket and returns the value when it becomes " +
            "available.",
        exampleCall = "`{apiObjectName}.readBoolean(\"Did you check this box?\");`",
        nullable = true
    )
    fun readBoolean(message: String): Boolean?

    @ScriptFunction(
        description = "Requests a number input from the user via web socket and returns the value when it becomes " +
            "available. Returns `null` for invalid values.",
        exampleCall = "`{apiObjectName}.readNumber(\"The answer is:\");`",
        nullable = true
    )
    fun readNumber(message: String): Double?

    @ScriptFunction(
        description = "Requests a number input from the user via web socket and returns the value when it becomes " +
            "available.",
        exampleCall = "`{apiObjectName}.readString(\"Name:\");`",
        nullable = true
    )
    fun readString(message: String): String?

    @ScriptFunction(
        description = "Requests multiple fields from the user. The fields can be specified via the `fields` argument " +
            "which is described below this table. The return value is a map which consists of field identifiers and " +
            "their values.",
        exampleCall = "Example given below.",
        nullable = true,
        signature = "<code>readFields(fields: Object, message: String): Map&lt;String, Boolean &#124; Number &#124; " +
            "String&gt; &#124; null</code>"
    )
    fun readFields(fields: Value, message: String): MapApi<String, Any>?

    @ScriptFunction(
        description = "Requests button click from the user via web socket and blocks execution until the user clicks " +
            "on the button. Does not block execution and returns immediately if the script is not running " +
            "interactively.",
        exampleCall = "`{apiObjectName}.button(\"Continue\");`",
        nullable = false
    )
    fun button(message: String)
}
