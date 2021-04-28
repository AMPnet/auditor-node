package com.ampnet.auditornode.scriptapi

interface JavaScriptApi {

    fun createJavaScriptApiObject(): String {
        val apiObjectName = javaClass.simpleName.toLowerCase()
        val fullClassName = javaClass.name
        return "const $apiObjectName = Java.type('$fullClassName');"
    }
}
