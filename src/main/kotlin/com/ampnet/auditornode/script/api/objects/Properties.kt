package com.ampnet.auditornode.script.api.objects

import com.ampnet.auditornode.configuration.properties.ScriptProperties
import javax.inject.Singleton

@Singleton
class Properties(private val scriptProperties: ScriptProperties) : JavaScriptApiObject {

    override fun createJavaScriptApiObject(): String {
        val environmentObject = scriptProperties.properties.map { entry ->
            "\"${entry.key}\":\"${entry.value}\""
        }.joinToString(separator = ",", prefix = "{", postfix = "}")
        return "const ${apiObjectName()} = $environmentObject;"
    }
}
