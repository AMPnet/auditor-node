package com.ampnet.auditornode

import com.ampnet.auditornode.scriptapi.Http
import com.ampnet.auditornode.scriptapi.JavaScriptApi
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source

fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "IPFS file hash should be provided as a program argument" }
    println("Input IPFS hash: " + args[0])

    val ipfsFile = Http.post("http://localhost:5001/api/v0/cat?arg=${args[0]}")
    // alternative ways to fetch from IPFS using a gateway:
    // val ipfsFile = Http.get("https://ipfs.io/ipfs/${args[0]}")

    requireNotNull(ipfsFile) { "No IPFS file content" }

    Context.newBuilder("js")
        .allowHostAccess(HostAccess.EXPLICIT)
        .allowHostClassLookup { fullClassName -> fullClassName.startsWith(JavaScriptApi::class.java.`package`.name) }
        .build()
        .use {
            val apiObjects = listOf(Http.createJavaScriptApiObject()).joinToString(separator = "\n")
            val scriptSource = "$apiObjects\n$ipfsFile"
            val source = Source.create("js", scriptSource)
            it.eval(source)
        }
}
