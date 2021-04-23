package com.ampnet.auditornode

import com.ampnet.auditornode.scriptapi.Http
import com.ampnet.auditornode.scriptapi.JavaScriptApi
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "At least one argument is required" }
    println("Input file path: " + args[0])

    Context.newBuilder("js")
        .allowHostAccess(HostAccess.EXPLICIT)
        .allowHostClassLookup { fullClassName -> fullClassName.startsWith(JavaScriptApi::class.java.`package`.name) }
        .build()
        .use {
            val apiObjects = listOf(Http.createJavaScriptApiObject())
            val lines = Files.readAllLines(Paths.get(args[0]))
            val scriptSource = (apiObjects + lines).joinToString(separator = "\n")
            val source = Source.create("js", scriptSource)
            it.eval(source)
        }
}
