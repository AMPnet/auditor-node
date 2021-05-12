package com.ampnet.auditornode

import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import javax.inject.Inject

abstract class ApiTestBase : TestBase() {

    @Inject
    @field:Client("/")
    // relative paths can be used in requests, e.g. just use "/hello"
    protected lateinit var client: RxHttpClient
}
