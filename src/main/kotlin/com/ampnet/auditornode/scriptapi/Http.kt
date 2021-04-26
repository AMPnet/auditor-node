package com.ampnet.auditornode.scriptapi

import okhttp3.OkHttpClient
import okhttp3.Request
import org.graalvm.polyglot.HostAccess.Export

object Http : JavaScriptApi {

    private val client = OkHttpClient()

    @Export
    @JvmStatic
    fun get(url: String): String? {
        val req = Request.Builder()
            .get()
            .url(url)
            .build()

        client.newCall(req).execute().use {
            return it.body?.string();
        }
    }

}
