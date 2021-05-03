package com.ampnet.auditornode.script.api

interface JavaScriptApi {

    fun apiObjectName(): String = javaClass.simpleName.toLowerCase()

    fun createJavaScriptApiObject(): String {
        val fullClassName = javaClass.name
        return "const ${apiObjectName()} = Java.type('$fullClassName');"
    }
}
