package com.ampnet.auditornode.scriptapi

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.EMPTY_REQUEST
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
            return it.body?.string()
        }
    }

    @Export
    @JvmStatic
    fun post(url: String): String? {
        val req = Request.Builder()
            .post(body = EMPTY_REQUEST)
            .url(url)
            .build()

        client.newCall(req).execute().use {
            return it.body?.string()
        }
    }
}
