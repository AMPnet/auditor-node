package com.ampnet.auditornode.model.script

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import org.graalvm.polyglot.HostAccess.Export
import javax.inject.Singleton

@Singleton
class Http(client: HttpClient) : JavaScriptApi { // TODO refactoring

    private val blockingClient = client.toBlocking()

    @Export
    fun get(url: String): String? {
        val request = HttpRequest.GET<String>(url)
        return blockingClient.retrieve(request)
    }

    @Export
    fun post(url: String): String? {
        val request = HttpRequest.POST(url, "")
        return blockingClient.retrieve(request)
    }
}
