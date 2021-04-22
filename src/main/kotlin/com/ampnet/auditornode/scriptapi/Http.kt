package com.ampnet.auditornode.scriptapi

import okhttp3.OkHttpClient
import okhttp3.Request
import org.graalvm.polyglot.HostAccess.Export

object Http {

    private val client = OkHttpClient()

    @Export
    @JvmStatic
    fun request(method: String, url: String): String? {
        val req = Request.Builder()
            .method(method, null)
            .url(url)
            .build()

        client.newCall(req).execute().use {
            val responseBody = it.body?.string()?.split("\n")?.firstOrNull()
            println("Got HTTP body:")
            println(responseBody)
            return responseBody
        }
    }

}
