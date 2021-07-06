package com.ampnet.auditornode.script.api.objects

import com.ampnet.auditornode.configuration.properties.ScriptProperties
import com.ampnet.auditornode.documentation.annotation.ScriptApi
import com.ampnet.auditornode.documentation.annotation.ScriptApiCategory
import com.ampnet.auditornode.documentation.annotation.ScriptField
import com.ampnet.auditornode.util.NativeReflection
import javax.inject.Singleton

@ScriptApi(
    description = "`{apiObjectName}` object contains all the application properties with `script.properties` prefix " +
        "which were defined as `-script.properties.<propertyName>=<propertyValue>` program arguments or otherwise. " +
        "An important note here is that all the defined property names will be visible through `kebab-case`. " +
        "For example, specifying `-script.properties.exampleProperty=exampleValue` will cause the `example-property` " +
        "to be visible in the `{apiObjectName}` object:\n\n" +
        "```javascript\n" +
        "console.log({apiObjectName}[\"example-property\"]); // prints out \"exampleValue\"\n" +
        "```",
    category = ScriptApiCategory.UTILITY,
    hasStaticApi = true,
    additionalFields = [
        ScriptField(
            description = "Value of the property under `<propertyName>` key.",
            signature = "`<propertyName>: String`"
        )
    ],
    fieldsDocumentationHeader = "The fields of this object are dynamic and depend on the available " +
        "`script.properties` values. All the fields will always be of the `String` type."
)
@Singleton
@NativeReflection
class Properties(private val scriptProperties: ScriptProperties) : JavaScriptApiObject {

    override fun createJavaScriptApiObject(): String {
        val environmentObject = scriptProperties.properties.map { entry ->
            "\"${entry.key}\":\"${entry.value}\""
        }.joinToString(separator = ",", prefix = "{", postfix = "}")
        return "const ${apiObjectName()} = $environmentObject;"
    }
}
