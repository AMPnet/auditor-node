package com.ampnet.auditornode.script.api.classes

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.ampnet.auditornode.ApiTestBase
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.model.error.EvaluationError.InvalidInputValueError
import com.ampnet.auditornode.script.api.model.AuditResult
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.request
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.cookie.Cookie
import io.micronaut.http.cookie.SameSite
import io.micronaut.http.simple.cookies.SimpleCookie
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MicronautTest
class HttpClientJavaScriptApiTest : ApiTestBase() {

    private val wireMockServer = WireMockServer(wireMockConfig().dynamicPort())

    @BeforeEach
    fun beforeEach() {
        wireMockServer.start()
    }

    @AfterEach
    fun afterEach() {
        wireMockServer.stop()
    }

    private fun Cookie.asResponseHeader(): HttpHeader {
        val headerValue = StringBuilder("$name=$value").apply {
            if (domain != null) {
                append("; Domain=").append(domain)
            }

            if (path != null) {
                append("; Path=").append(path)
            }

            if (isHttpOnly) {
                append("; HttpOnly")
            }

            if (isSecure) {
                append("; Secure")
            }

            if (maxAge > 0) {
                append("; Max-Age=").append(maxAge)
            }

            sameSite.ifPresent { append("; SameSite=").append(it) }
        }.toString()

        return HttpHeader("Set-Cookie", headerValue)
    }

    @Test
    fun `must execute script which uses simple get() call`() {
        val responseHeaders = mapOf(
            "header1" to "value1",
            "header2" to "value2"
        )
        val responseCookie = SimpleCookie("cookieName", "cookieValue").apply {
            domain("localhost")
            path("/slash")
            httpOnly(true)
            secure(true)
            maxAge(1000L)
            sameSite(SameSite.Strict)
        }
        val wireMockHeaders = HttpHeaders(responseHeaders.map { HttpHeader(it.key, it.value) })
            .plus(responseCookie.asResponseHeader())
        val responseBody = "{}"
        val responseCode = 200

        suppose("some mock HTTP response will be returned") {
            wireMockServer.stubFor(
                get(urlPathEqualTo("/example"))
                    .willReturn(
                        aResponse()
                            .withBody(responseBody)
                            .withHeaders(wireMockHeaders)
                            .withStatus(responseCode)
                    )
            )
        }

        verify("correct response body is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    let response = HttpClient.get("http://localhost:${wireMockServer.port()}/example");

                    assertEquals("response.body", "$responseBody", response.body);
                    assertEquals("response.statusCode", $responseCode, response.statusCode);
                    assertNonNull("response.headers.size", response.headers.size);

                    let header1List = response.headers.get("header1");

                    assertNonNull("response.headers.get(\"header1\")", header1List);
                    assertEquals("header1List.length", 1, header1List.length);
                    assertEquals("header1List.get(0)", "${responseHeaders["header1"]}", header1List.get(0));

                    let header2List = response.headers.get("header2");

                    assertNonNull("response.headers.get(\"header2\")", header2List);
                    assertEquals("header2List.length", 1, header2List.length);
                    assertEquals("header2List.get(0)", "${responseHeaders["header2"]}", header2List.get(0));

                    assertEquals("response.cookies.length", 1, response.cookies.length);

                    let cookie = response.cookies.get(0);

                    assertEquals("cookie.name", "${responseCookie.name}", cookie.name);
                    assertEquals("cookie.value", "${responseCookie.value}", cookie.value);
                    assertEquals("cookie.domain", "${responseCookie.domain}", cookie.domain);
                    assertEquals("cookie.path", "${responseCookie.path}", cookie.path);
                    assertEquals("cookie.httpOnly", ${responseCookie.isHttpOnly}, cookie.httpOnly);
                    assertEquals("cookie.secure", ${responseCookie.isSecure}, cookie.secure);
                    assertEquals("cookie.maxAge", ${responseCookie.maxAge}, cookie.maxAge);
                    assertEquals("cookie.sameSite", "${responseCookie.sameSite.get().name}", cookie.sameSite);

                    return AuditResult.of(true);
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/execute", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            assertThat(result).isEqualTo(AuditResult(true).right().toString())
        }
    }

    @Test
    fun `must execute script which uses get() call with some headers`() {
        val responseHeaders = mapOf(
            "header1" to "value1",
            "header2" to "value2"
        )
        val responseCookie = SimpleCookie("cookieName", "cookieValue").apply {
            domain("example.com")
            path("/slash")
            httpOnly(true)
            secure(true)
            maxAge(1000L)
            sameSite(SameSite.Strict)
        }
        val wireMockHeaders = HttpHeaders(responseHeaders.map { HttpHeader(it.key, it.value) })
            .plus(responseCookie.asResponseHeader())
        val responseBody = "{}"
        val responseCode = 200

        suppose("some mock HTTP response will be returned") {
            wireMockServer.stubFor(
                get(urlPathEqualTo("/example"))
                    .withHeader("requestHeader1", equalTo("headerValue1"))
                    .withHeader("requestHeader2", equalTo("headerValue2"))
                    .willReturn(
                        aResponse()
                            .withBody(responseBody)
                            .withHeaders(wireMockHeaders)
                            .withStatus(responseCode)
                    )
            )
        }

        verify("correct response body is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    let response = HttpClient.get(
                        "http://localhost:${wireMockServer.port()}/example",
                        {
                            requestHeader1: "headerValue1",
                            requestHeader2: "headerValue2"
                        }
                    );

                    assertEquals("response.body", "$responseBody", response.body);
                    assertEquals("response.statusCode", $responseCode, response.statusCode);
                    assertNonNull("response.headers.size", response.headers.size);

                    let header1List = response.headers.get("header1");

                    assertNonNull("response.headers.get(\"header1\")", header1List);
                    assertEquals("header1List.length", 1, header1List.length);
                    assertEquals("header1List.get(0)", "${responseHeaders["header1"]}", header1List.get(0));

                    let header2List = response.headers.get("header2");

                    assertNonNull("response.headers.get(\"header2\")", header2List);
                    assertEquals("header2List.length", 1, header2List.length);
                    assertEquals("header2List.get(0)", "${responseHeaders["header2"]}", header2List.get(0));

                    assertEquals("response.cookies.length", 1, response.cookies.length);

                    let cookie = response.cookies.get(0);

                    assertEquals("cookie.name", "${responseCookie.name}", cookie.name);
                    assertEquals("cookie.value", "${responseCookie.value}", cookie.value);
                    assertEquals("cookie.domain", "${responseCookie.domain}", cookie.domain);
                    assertEquals("cookie.path", "${responseCookie.path}", cookie.path);
                    assertEquals("cookie.httpOnly", ${responseCookie.isHttpOnly}, cookie.httpOnly);
                    assertEquals("cookie.secure", ${responseCookie.isSecure}, cookie.secure);
                    assertEquals("cookie.maxAge", ${responseCookie.maxAge}, cookie.maxAge);
                    assertEquals("cookie.sameSite", "${responseCookie.sameSite.get().name}", cookie.sameSite);

                    return AuditResult.of(true);
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/execute", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            assertThat(result).isEqualTo(AuditResult(true).right().toString())
        }
    }

    @Test
    fun `must execute script which uses get() call with invalid headers argument`() {
        val expectedException = InvalidInputValueError(
            methodCall = "HttpClient.get()",
            argumentIndex = 1,
            expectedType = "<JavaScript object>",
            actualType = "headers"
        ).toString()

        verify("exception is thrown for invalid headers argument") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    assertThrows(
                        "HttpClient.get()",
                        "$expectedException",
                        function() { HttpClient.get("http://localhost:${wireMockServer.port()}/example", "headers"); }
                    );

                    return AuditResult.of(true);
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/execute", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            assertThat(result).isEqualTo(AuditResult(true).right().toString())
        }
    }

    @Test
    fun `must execute script which uses simple post() call`() {
        val responseHeaders = mapOf(
            "header1" to "value1",
            "header2" to "value2"
        )
        val responseCookie = SimpleCookie("cookieName", "cookieValue").apply {
            domain("example.com")
            path("/slash")
            httpOnly(true)
            secure(true)
            maxAge(1000L)
            sameSite(SameSite.Strict)
        }
        val wireMockHeaders = HttpHeaders(responseHeaders.map { HttpHeader(it.key, it.value) })
            .plus(responseCookie.asResponseHeader())
        val responseBody = "{}"
        val responseCode = 200

        suppose("some mock HTTP response will be returned") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/example"))
                    .willReturn(
                        aResponse()
                            .withBody(responseBody)
                            .withHeaders(wireMockHeaders)
                            .withStatus(responseCode)
                    )
            )
        }

        verify("correct response body is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    let response = HttpClient.post("http://localhost:${wireMockServer.port()}/example");

                    assertEquals("response.body", "$responseBody", response.body);
                    assertEquals("response.statusCode", $responseCode, response.statusCode);
                    assertNonNull("response.headers.size", response.headers.size);

                    let header1List = response.headers.get("header1");

                    assertNonNull("response.headers.get(\"header1\")", header1List);
                    assertEquals("header1List.length", 1, header1List.length);
                    assertEquals("header1List.get(0)", "${responseHeaders["header1"]}", header1List.get(0));

                    let header2List = response.headers.get("header2");

                    assertNonNull("response.headers.get(\"header2\")", header2List);
                    assertEquals("header2List.length", 1, header2List.length);
                    assertEquals("header2List.get(0)", "${responseHeaders["header2"]}", header2List.get(0));

                    assertEquals("response.cookies.length", 1, response.cookies.length);

                    let cookie = response.cookies.get(0);

                    assertEquals("cookie.name", "${responseCookie.name}", cookie.name);
                    assertEquals("cookie.value", "${responseCookie.value}", cookie.value);
                    assertEquals("cookie.domain", "${responseCookie.domain}", cookie.domain);
                    assertEquals("cookie.path", "${responseCookie.path}", cookie.path);
                    assertEquals("cookie.httpOnly", ${responseCookie.isHttpOnly}, cookie.httpOnly);
                    assertEquals("cookie.secure", ${responseCookie.isSecure}, cookie.secure);
                    assertEquals("cookie.maxAge", ${responseCookie.maxAge}, cookie.maxAge);
                    assertEquals("cookie.sameSite", "${responseCookie.sameSite.get().name}", cookie.sameSite);

                    return AuditResult.of(true);
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/execute", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            assertThat(result).isEqualTo(AuditResult(true).right().toString())
        }
    }

    @Test
    fun `must execute script which uses post() call with some request body`() {
        val responseHeaders = mapOf(
            "header1" to "value1",
            "header2" to "value2"
        )
        val responseCookie = SimpleCookie("cookieName", "cookieValue").apply {
            domain("example.com")
            path("/slash")
            httpOnly(true)
            secure(true)
            maxAge(1000L)
            sameSite(SameSite.Strict)
        }
        val wireMockHeaders = HttpHeaders(responseHeaders.map { HttpHeader(it.key, it.value) })
            .plus(responseCookie.asResponseHeader())
        val responseBody = "{}"
        val responseCode = 200
        val requestBody = "exampleRequest"

        suppose("some mock HTTP response will be returned") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/example"))
                    .withRequestBody(equalTo(requestBody))
                    .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON))
                    .willReturn(
                        aResponse()
                            .withBody(responseBody)
                            .withHeaders(wireMockHeaders)
                            .withStatus(responseCode)
                    )
            )
        }

        verify("correct response body is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    let response = HttpClient.post(
                        "http://localhost:${wireMockServer.port()}/example",
                        "$requestBody"
                    );

                    assertEquals("response.body", "$responseBody", response.body);
                    assertEquals("response.statusCode", $responseCode, response.statusCode);
                    assertNonNull("response.headers.size", response.headers.size);

                    let header1List = response.headers.get("header1");

                    assertNonNull("response.headers.get(\"header1\")", header1List);
                    assertEquals("header1List.length", 1, header1List.length);
                    assertEquals("header1List.get(0)", "${responseHeaders["header1"]}", header1List.get(0));

                    let header2List = response.headers.get("header2");

                    assertNonNull("response.headers.get(\"header2\")", header2List);
                    assertEquals("header2List.length", 1, header2List.length);
                    assertEquals("header2List.get(0)", "${responseHeaders["header2"]}", header2List.get(0));

                    assertEquals("response.cookies.length", 1, response.cookies.length);

                    let cookie = response.cookies.get(0);

                    assertEquals("cookie.name", "${responseCookie.name}", cookie.name);
                    assertEquals("cookie.value", "${responseCookie.value}", cookie.value);
                    assertEquals("cookie.domain", "${responseCookie.domain}", cookie.domain);
                    assertEquals("cookie.path", "${responseCookie.path}", cookie.path);
                    assertEquals("cookie.httpOnly", ${responseCookie.isHttpOnly}, cookie.httpOnly);
                    assertEquals("cookie.secure", ${responseCookie.isSecure}, cookie.secure);
                    assertEquals("cookie.maxAge", ${responseCookie.maxAge}, cookie.maxAge);
                    assertEquals("cookie.sameSite", "${responseCookie.sameSite.get().name}", cookie.sameSite);

                    return AuditResult.of(true);
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/execute", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            assertThat(result).isEqualTo(AuditResult(true).right().toString())
        }
    }

    @Test
    fun `must execute script which uses post() call with some headers`() {
        val responseHeaders = mapOf(
            "header1" to "value1",
            "header2" to "value2"
        )
        val responseCookie = SimpleCookie("cookieName", "cookieValue").apply {
            domain("example.com")
            path("/slash")
            httpOnly(true)
            secure(true)
            maxAge(1000L)
            sameSite(SameSite.Strict)
        }
        val wireMockHeaders = HttpHeaders(responseHeaders.map { HttpHeader(it.key, it.value) })
            .plus(responseCookie.asResponseHeader())
        val responseBody = "{}"
        val responseCode = 200
        val requestBody = "exampleRequest"

        suppose("some mock HTTP response will be returned") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/example"))
                    .withRequestBody(equalTo(requestBody))
                    .withHeader("Content-Type", equalTo(MediaType.TEXT_PLAIN))
                    .withHeader("requestHeader1", equalTo("headerValue1"))
                    .withHeader("requestHeader2", equalTo("headerValue2"))
                    .willReturn(
                        aResponse()
                            .withBody(responseBody)
                            .withHeaders(wireMockHeaders)
                            .withStatus(responseCode)
                    )
            )
        }

        verify("correct response body is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    let response = HttpClient.post(
                        "http://localhost:${wireMockServer.port()}/example",
                        "$requestBody",
                        {
                            "Content-Type": "text/plain",
                            requestHeader1: "headerValue1",
                            requestHeader2: "headerValue2"
                        }
                    );

                    assertEquals("response.body", "$responseBody", response.body);
                    assertEquals("response.statusCode", $responseCode, response.statusCode);
                    assertNonNull("response.headers.size", response.headers.size);

                    let header1List = response.headers.get("header1");

                    assertNonNull("response.headers.get(\"header1\")", header1List);
                    assertEquals("header1List.length", 1, header1List.length);
                    assertEquals("header1List.get(0)", "${responseHeaders["header1"]}", header1List.get(0));

                    let header2List = response.headers.get("header2");

                    assertNonNull("response.headers.get(\"header2\")", header2List);
                    assertEquals("header2List.length", 1, header2List.length);
                    assertEquals("header2List.get(0)", "${responseHeaders["header2"]}", header2List.get(0));

                    assertEquals("response.cookies.length", 1, response.cookies.length);

                    let cookie = response.cookies.get(0);

                    assertEquals("cookie.name", "${responseCookie.name}", cookie.name);
                    assertEquals("cookie.value", "${responseCookie.value}", cookie.value);
                    assertEquals("cookie.domain", "${responseCookie.domain}", cookie.domain);
                    assertEquals("cookie.path", "${responseCookie.path}", cookie.path);
                    assertEquals("cookie.httpOnly", ${responseCookie.isHttpOnly}, cookie.httpOnly);
                    assertEquals("cookie.secure", ${responseCookie.isSecure}, cookie.secure);
                    assertEquals("cookie.maxAge", ${responseCookie.maxAge}, cookie.maxAge);
                    assertEquals("cookie.sameSite", "${responseCookie.sameSite.get().name}", cookie.sameSite);

                    return AuditResult.of(true);
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/execute", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            assertThat(result).isEqualTo(AuditResult(true).right().toString())
        }
    }

    @Test
    fun `must execute script which uses post() call with invalid headers argument`() {
        val expectedException = InvalidInputValueError(
            methodCall = "HttpClient.post()",
            argumentIndex = 2,
            expectedType = "<JavaScript object>",
            actualType = "headers"
        ).toString()

        verify("exception is thrown for invalid headers argument") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    assertThrows(
                        "HttpClient.post()",
                        "$expectedException",
                        function() {
                            HttpClient.post(
                                "http://localhost:${wireMockServer.port()}/example",
                                "{\"requestBody\":true}",
                                "headers"
                            );
                        }
                    );

                    return AuditResult.of(true);
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/execute", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            assertThat(result).isEqualTo(AuditResult(true).right().toString())
        }
    }

    @Test
    fun `must execute script which uses simple request() call`() {
        val responseHeaders = mapOf(
            "header1" to "value1",
            "header2" to "value2"
        )
        val responseCookie = SimpleCookie("cookieName", "cookieValue").apply {
            domain("example.com")
            path("/slash")
            httpOnly(true)
            secure(true)
            maxAge(1000L)
            sameSite(SameSite.Strict)
        }
        val wireMockHeaders = HttpHeaders(responseHeaders.map { HttpHeader(it.key, it.value) })
            .plus(responseCookie.asResponseHeader())
        val responseBody = "{}"
        val responseCode = 200
        val customMethodName = "NON_STANDARD_METHOD"

        suppose("some mock HTTP response will be returned") {
            wireMockServer.stubFor(
                request(customMethodName, urlPathEqualTo("/example"))
                    .willReturn(
                        aResponse()
                            .withBody(responseBody)
                            .withHeaders(wireMockHeaders)
                            .withStatus(responseCode)
                    )
            )
        }

        verify("correct response body is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    let response = HttpClient.request(
                        "http://localhost:${wireMockServer.port()}/example",
                        "$customMethodName"
                    );

                    assertEquals("response.body", "$responseBody", response.body);
                    assertEquals("response.statusCode", $responseCode, response.statusCode);
                    assertNonNull("response.headers.size", response.headers.size);

                    let header1List = response.headers.get("header1");

                    assertNonNull("response.headers.get(\"header1\")", header1List);
                    assertEquals("header1List.length", 1, header1List.length);
                    assertEquals("header1List.get(0)", "${responseHeaders["header1"]}", header1List.get(0));

                    let header2List = response.headers.get("header2");

                    assertNonNull("response.headers.get(\"header2\")", header2List);
                    assertEquals("header2List.length", 1, header2List.length);
                    assertEquals("header2List.get(0)", "${responseHeaders["header2"]}", header2List.get(0));

                    assertEquals("response.cookies.length", 1, response.cookies.length);

                    let cookie = response.cookies.get(0);

                    assertEquals("cookie.name", "${responseCookie.name}", cookie.name);
                    assertEquals("cookie.value", "${responseCookie.value}", cookie.value);
                    assertEquals("cookie.domain", "${responseCookie.domain}", cookie.domain);
                    assertEquals("cookie.path", "${responseCookie.path}", cookie.path);
                    assertEquals("cookie.httpOnly", ${responseCookie.isHttpOnly}, cookie.httpOnly);
                    assertEquals("cookie.secure", ${responseCookie.isSecure}, cookie.secure);
                    assertEquals("cookie.maxAge", ${responseCookie.maxAge}, cookie.maxAge);
                    assertEquals("cookie.sameSite", "${responseCookie.sameSite.get().name}", cookie.sameSite);

                    return AuditResult.of(true);
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/execute", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            assertThat(result).isEqualTo(AuditResult(true).right().toString())
        }
    }

    @Test
    fun `must execute script which uses request() call with some request body`() {
        val responseHeaders = mapOf(
            "header1" to "value1",
            "header2" to "value2"
        )
        val responseCookie = SimpleCookie("cookieName", "cookieValue").apply {
            domain("example.com")
            path("/slash")
            httpOnly(true)
            secure(true)
            maxAge(1000L)
            sameSite(SameSite.Strict)
        }
        val wireMockHeaders = HttpHeaders(responseHeaders.map { HttpHeader(it.key, it.value) })
            .plus(responseCookie.asResponseHeader())
        val responseBody = "{}"
        val responseCode = 200
        val customMethodName = "NON_STANDARD_METHOD"
        val requestBody = "exampleRequest"

        suppose("some mock HTTP response will be returned") {
            wireMockServer.stubFor(
                request(customMethodName, urlPathEqualTo("/example"))
                    .withRequestBody(equalTo(requestBody))
                    .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON))
                    .willReturn(
                        aResponse()
                            .withBody(responseBody)
                            .withHeaders(wireMockHeaders)
                            .withStatus(responseCode)
                    )
            )
        }

        verify("correct response body is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    let response = HttpClient.request(
                        "http://localhost:${wireMockServer.port()}/example",
                        "$customMethodName",
                        "$requestBody"
                    );

                    assertEquals("response.body", "$responseBody", response.body);
                    assertEquals("response.statusCode", $responseCode, response.statusCode);
                    assertNonNull("response.headers.size", response.headers.size);

                    let header1List = response.headers.get("header1");

                    assertNonNull("response.headers.get(\"header1\")", header1List);
                    assertEquals("header1List.length", 1, header1List.length);
                    assertEquals("header1List.get(0)", "${responseHeaders["header1"]}", header1List.get(0));

                    let header2List = response.headers.get("header2");

                    assertNonNull("response.headers.get(\"header2\")", header2List);
                    assertEquals("header2List.length", 1, header2List.length);
                    assertEquals("header2List.get(0)", "${responseHeaders["header2"]}", header2List.get(0));

                    assertEquals("response.cookies.length", 1, response.cookies.length);

                    let cookie = response.cookies.get(0);

                    assertEquals("cookie.name", "${responseCookie.name}", cookie.name);
                    assertEquals("cookie.value", "${responseCookie.value}", cookie.value);
                    assertEquals("cookie.domain", "${responseCookie.domain}", cookie.domain);
                    assertEquals("cookie.path", "${responseCookie.path}", cookie.path);
                    assertEquals("cookie.httpOnly", ${responseCookie.isHttpOnly}, cookie.httpOnly);
                    assertEquals("cookie.secure", ${responseCookie.isSecure}, cookie.secure);
                    assertEquals("cookie.maxAge", ${responseCookie.maxAge}, cookie.maxAge);
                    assertEquals("cookie.sameSite", "${responseCookie.sameSite.get().name}", cookie.sameSite);

                    return AuditResult.of(true);
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/execute", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            assertThat(result).isEqualTo(AuditResult(true).right().toString())
        }
    }

    @Test
    fun `must execute script which uses request() call with some headers`() {
        val responseHeaders = mapOf(
            "header1" to "value1",
            "header2" to "value2"
        )
        val responseCookie = SimpleCookie("cookieName", "cookieValue").apply {
            domain("example.com")
            path("/slash")
            httpOnly(true)
            secure(true)
            maxAge(1000L)
            sameSite(SameSite.Strict)
        }
        val wireMockHeaders = HttpHeaders(responseHeaders.map { HttpHeader(it.key, it.value) })
            .plus(responseCookie.asResponseHeader())
        val responseBody = "{}"
        val responseCode = 200
        val customMethodName = "NON_STANDARD_METHOD"
        val requestBody = "exampleRequest"

        suppose("some mock HTTP response will be returned") {
            wireMockServer.stubFor(
                request(customMethodName, urlPathEqualTo("/example"))
                    .withRequestBody(equalTo(requestBody))
                    .withHeader("Content-Type", equalTo(MediaType.TEXT_PLAIN))
                    .withHeader("requestHeader1", equalTo("headerValue1"))
                    .withHeader("requestHeader2", equalTo("headerValue2"))
                    .willReturn(
                        aResponse()
                            .withBody(responseBody)
                            .withHeaders(wireMockHeaders)
                            .withStatus(responseCode)
                    )
            )
        }

        verify("correct response body is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    let response = HttpClient.request(
                        "http://localhost:${wireMockServer.port()}/example",
                        "$customMethodName",
                        "$requestBody",
                        {
                            "Content-Type": "text/plain",
                            requestHeader1: "headerValue1",
                            requestHeader2: "headerValue2"
                        }
                    );

                    assertEquals("response.body", "$responseBody", response.body);
                    assertEquals("response.statusCode", $responseCode, response.statusCode);
                    assertNonNull("response.headers.size", response.headers.size);

                    let header1List = response.headers.get("header1");

                    assertNonNull("response.headers.get(\"header1\")", header1List);
                    assertEquals("header1List.length", 1, header1List.length);
                    assertEquals("header1List.get(0)", "${responseHeaders["header1"]}", header1List.get(0));

                    let header2List = response.headers.get("header2");

                    assertNonNull("response.headers.get(\"header2\")", header2List);
                    assertEquals("header2List.length", 1, header2List.length);
                    assertEquals("header2List.get(0)", "${responseHeaders["header2"]}", header2List.get(0));

                    assertEquals("response.cookies.length", 1, response.cookies.length);

                    let cookie = response.cookies.get(0);

                    assertEquals("cookie.name", "${responseCookie.name}", cookie.name);
                    assertEquals("cookie.value", "${responseCookie.value}", cookie.value);
                    assertEquals("cookie.domain", "${responseCookie.domain}", cookie.domain);
                    assertEquals("cookie.path", "${responseCookie.path}", cookie.path);
                    assertEquals("cookie.httpOnly", ${responseCookie.isHttpOnly}, cookie.httpOnly);
                    assertEquals("cookie.secure", ${responseCookie.isSecure}, cookie.secure);
                    assertEquals("cookie.maxAge", ${responseCookie.maxAge}, cookie.maxAge);
                    assertEquals("cookie.sameSite", "${responseCookie.sameSite.get().name}", cookie.sameSite);

                    return AuditResult.of(true);
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/execute", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            assertThat(result).isEqualTo(AuditResult(true).right().toString())
        }
    }

    @Test
    fun `must execute script which uses request() call with invalid headers argument`() {
        val expectedException = InvalidInputValueError(
            methodCall = "HttpClient.request()",
            argumentIndex = 3,
            expectedType = "<JavaScript object>",
            actualType = "headers"
        ).toString()

        verify("exception is thrown for invalid headers argument") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    assertThrows(
                        "HttpClient.request()",
                        "$expectedException",
                        function() {
                            HttpClient.request(
                                "http://localhost:${wireMockServer.port()}/example",
                                "NON_STANDARD_METHOD",
                                "{\"requestBody\":true}",
                                "headers"
                            );
                        }
                    );

                    return AuditResult.of(true);
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("/script/execute", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            assertThat(result).isEqualTo(AuditResult(true).right().toString())
        }
    }
}
