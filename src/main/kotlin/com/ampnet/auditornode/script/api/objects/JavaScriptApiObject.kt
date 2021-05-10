package com.ampnet.auditornode.script.api.objects

interface JavaScriptApiObject {

    fun apiObjectName(): String = javaClass.simpleName

    fun createJavaScriptApiObject(): String {
        val fullClassName = javaClass.name
        return "const ${apiObjectName()} = Java.type('$fullClassName');"
    }
}
