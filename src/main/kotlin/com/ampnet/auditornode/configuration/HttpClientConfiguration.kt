package com.ampnet.auditornode.configuration

import io.micronaut.context.annotation.Factory
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Factory
class HttpClientConfiguration {

    @Inject
    @Singleton
    fun blockingHttpClient(httpClient: HttpClient): BlockingHttpClient {
        return httpClient.toBlocking()
    }
}
