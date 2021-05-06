package com.ampnet.auditornode.model.script

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.BlockingHttpClient
import org.graalvm.polyglot.HostAccess.Export
import javax.inject.Singleton

@Singleton
class Http(private val blockingHttpClient: BlockingHttpClient) {

    @Export
    fun get(url: String): String? {
        val request = HttpRequest.GET<String>(url)
        return blockingHttpClient.retrieve(request)
    }

    @Export
    fun post(url: String): String? { // TODO add request body to API
        val request = HttpRequest.POST(url, "")
        return blockingHttpClient.retrieve(request)
    }
}
