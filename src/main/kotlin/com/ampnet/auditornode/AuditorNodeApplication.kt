package com.ampnet.auditornode

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess

fun main(args: Array<String>) {
    println("Hello from Kotlin!")

    Context.newBuilder("js")
        .allowHostAccess(HostAccess.EXPLICIT)
        .allowHostClassLookup { fullClassName -> fullClassName.startsWith("com.ampnet.auditornode.scriptapi") }
        .build()
        .use {
            it.eval("js", JS_CODE)
        }
}
