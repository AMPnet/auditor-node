package com.ampnet.auditornode.script.api.classes

import assertk.assertThat
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.model.error.EvaluationError.InvalidInputValueError
import com.ampnet.auditornode.script.api.model.AuditResult
import com.ampnet.auditornode.script.api.objects.Properties
import com.ampnet.auditornode.service.impl.JavaScriptAuditingService
import io.micronaut.core.convert.DefaultConversionService
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.cookie.SameSite
import io.micronaut.http.simple.SimpleHttpHeaders
import io.micronaut.http.simple.cookies.SimpleCookie
import io.micronaut.http.simple.cookies.SimpleCookies
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import java.net.URI

class HttpClientJavaScriptApiTest : TestBase() {

    private val properties = Properties(
        mock {
            on { properties } doReturn emptyMap()
        }
    )

    @Test
    fun `must correctly perform simple get() call`() {
        val client = mock<BlockingHttpClient>()
        val requestUri = URI.create("http://localhost:8080")
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
        val responseBody = "{}"
        val responseCode = 200

        suppose("client will return some HTTP response") {
            val httpRequestMatcher: (HttpRequest<String>) -> Boolean = { arg ->
                arg.uri == requestUri && arg.method == HttpMethod.GET && !arg.contentType.isPresent
            }
            val mockResponse = mock<io.micronaut.http.HttpResponse<String>> {
                on { headers } doReturn SimpleHttpHeaders(responseHeaders, DefaultConversionService())
                on { cookies } doReturn SimpleCookies(DefaultConversionService()).apply {
                    put(responseCookie.name, responseCookie)
                }
                on { body() } doReturn responseBody
                on { code() } doReturn responseCode
            }
            given(client.exchange(argThat(httpRequestMatcher), eq(String::class.java)))
                .willReturn(mockResponse)
        }

        val service = JavaScriptAuditingService(HttpClient(client), properties)

        verify("correct response body is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    let response = HttpClient.get("http://localhost:8080");

                    assertEquals("response.body", "$responseBody", response.body);
                    assertEquals("response.statusCode", $responseCode, response.statusCode);
                    assertEquals("response.headers.size", ${responseHeaders.size}, response.headers.size);

                    let header1List = response.headers.get("header1");

                    assertEquals("header1List.length", 1, header1List.length);
                    assertEquals("header1List.get(0)", "${responseHeaders["header1"]}", header1List.get(0));

                    let header2List = response.headers.get("header2");

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
            val result = service.evaluate(scriptSource)
            assertThat(result).isRightContaining(AuditResult(true))
        }
    }

    @Test
    fun `must correctly perform get() call with some headers`() {
        val client = mock<BlockingHttpClient>()
        val requestUri = URI.create("http://localhost:8080")
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
        val responseBody = "{}"
        val responseCode = 200

        suppose("client will return some HTTP response") {
            val httpRequestMatcher: (HttpRequest<String>) -> Boolean = { arg ->
                val headersMap = arg.headers.asMap()
                arg.uri == requestUri && arg.method == HttpMethod.GET && headersMap.size == 2 &&
                    headersMap["requestHeader1"]?.size == 1 && headersMap["requestHeader1"]!![0] == "headerValue1" &&
                    headersMap["requestHeader2"]?.size == 1 && headersMap["requestHeader2"]!![0] == "headerValue2" &&
                    !arg.contentType.isPresent
            }
            val mockResponse = mock<io.micronaut.http.HttpResponse<String>> {
                on { headers } doReturn SimpleHttpHeaders(responseHeaders, DefaultConversionService())
                on { cookies } doReturn SimpleCookies(DefaultConversionService()).apply {
                    put(responseCookie.name, responseCookie)
                }
                on { body() } doReturn responseBody
                on { code() } doReturn responseCode
            }
            given(client.exchange(argThat(httpRequestMatcher), eq(String::class.java)))
                .willReturn(mockResponse)
        }

        val service = JavaScriptAuditingService(HttpClient(client), properties)

        verify("correct response body is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    let response = HttpClient.get(
                        "http://localhost:8080",
                        {
                            requestHeader1: "headerValue1",
                            requestHeader2: "headerValue2"
                        }
                    );

                    assertEquals("response.body", "$responseBody", response.body);
                    assertEquals("response.statusCode", $responseCode, response.statusCode);
                    assertEquals("response.headers.size", ${responseHeaders.size}, response.headers.size);

                    let header1List = response.headers.get("header1");

                    assertEquals("header1List.length", 1, header1List.length);
                    assertEquals("header1List.get(0)", "${responseHeaders["header1"]}", header1List.get(0));

                    let header2List = response.headers.get("header2");

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
            val result = service.evaluate(scriptSource)
            assertThat(result).isRightContaining(AuditResult(true))
        }
    }

    @Test
    fun `must throw exception for get() call with invalid headers argument`() {
        val client = mock<BlockingHttpClient>()
        val service = JavaScriptAuditingService(HttpClient(client), properties)
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
                        function() { HttpClient.get("http://localhost:8080", "headers"); }
                    );

                    return AuditResult.of(true);
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource)
            assertThat(result).isRightContaining(AuditResult(true))
        }
    }

    @Test
    fun `must correctly perform simple post() call`() {
        val client = mock<BlockingHttpClient>()
        val requestUri = URI.create("http://localhost:8080")
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
        val responseBody = "{}"
        val responseCode = 200

        suppose("client will return some HTTP response") {
            val httpRequestMatcher: (HttpRequest<String>) -> Boolean = { arg ->
                arg.uri == requestUri && arg.method == HttpMethod.POST && arg.body.get().isEmpty() &&
                    !arg.contentType.isPresent
            }
            val mockResponse = mock<io.micronaut.http.HttpResponse<String>> {
                on { headers } doReturn SimpleHttpHeaders(responseHeaders, DefaultConversionService())
                on { cookies } doReturn SimpleCookies(DefaultConversionService()).apply {
                    put(responseCookie.name, responseCookie)
                }
                on { body() } doReturn responseBody
                on { code() } doReturn responseCode
            }
            given(client.exchange(argThat(httpRequestMatcher), eq(String::class.java)))
                .willReturn(mockResponse)
        }

        val service = JavaScriptAuditingService(HttpClient(client), properties)

        verify("correct response body is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    let response = HttpClient.post("http://localhost:8080");

                    assertEquals("response.body", "$responseBody", response.body);
                    assertEquals("response.statusCode", $responseCode, response.statusCode);
                    assertEquals("response.headers.size", ${responseHeaders.size}, response.headers.size);

                    let header1List = response.headers.get("header1");

                    assertEquals("header1List.length", 1, header1List.length);
                    assertEquals("header1List.get(0)", "${responseHeaders["header1"]}", header1List.get(0));

                    let header2List = response.headers.get("header2");

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
            val result = service.evaluate(scriptSource)
            assertThat(result).isRightContaining(AuditResult(true))
        }
    }

    @Test
    fun `must correctly perform post() call with some request body`() {
        val client = mock<BlockingHttpClient>()
        val requestUri = URI.create("http://localhost:8080")
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
        val responseBody = "{}"
        val responseCode = 200
        val requestBody = "exampleRequest"

        suppose("client will return some HTTP response") {
            val httpRequestMatcher: (HttpRequest<String>) -> Boolean = { arg ->
                arg.uri == requestUri && arg.method == HttpMethod.POST && arg.body.get() == requestBody &&
                    arg.contentType.get() == MediaType.APPLICATION_JSON_TYPE
            }
            val mockResponse = mock<io.micronaut.http.HttpResponse<String>> {
                on { headers } doReturn SimpleHttpHeaders(responseHeaders, DefaultConversionService())
                on { cookies } doReturn SimpleCookies(DefaultConversionService()).apply {
                    put(responseCookie.name, responseCookie)
                }
                on { body() } doReturn responseBody
                on { code() } doReturn responseCode
            }
            given(client.exchange(argThat(httpRequestMatcher), eq(String::class.java)))
                .willReturn(mockResponse)
        }

        val service = JavaScriptAuditingService(HttpClient(client), properties)

        verify("correct response body is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    let response = HttpClient.post("http://localhost:8080", "$requestBody");

                    assertEquals("response.body", "$responseBody", response.body);
                    assertEquals("response.statusCode", $responseCode, response.statusCode);
                    assertEquals("response.headers.size", ${responseHeaders.size}, response.headers.size);

                    let header1List = response.headers.get("header1");

                    assertEquals("header1List.length", 1, header1List.length);
                    assertEquals("header1List.get(0)", "${responseHeaders["header1"]}", header1List.get(0));

                    let header2List = response.headers.get("header2");

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
            val result = service.evaluate(scriptSource)
            assertThat(result).isRightContaining(AuditResult(true))
        }
    }

    @Test
    fun `must correctly perform post() call with some headers`() {
        val client = mock<BlockingHttpClient>()
        val requestUri = URI.create("http://localhost:8080")
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
        val responseBody = "{}"
        val responseCode = 200
        val requestBody = "exampleRequest"

        suppose("client will return some HTTP response") {
            val httpRequestMatcher: (HttpRequest<String>) -> Boolean = { arg ->
                val headersMap = arg.headers.asMap()
                arg.uri == requestUri && arg.method == HttpMethod.POST && arg.body.get() == requestBody &&
                    headersMap.size == 3 &&
                    headersMap["Content-Type"]?.size == 1 && headersMap["Content-Type"]!![0] == "text/plain" &&
                    headersMap["requestHeader1"]?.size == 1 && headersMap["requestHeader1"]!![0] == "headerValue1" &&
                    headersMap["requestHeader2"]?.size == 1 && headersMap["requestHeader2"]!![0] == "headerValue2" &&
                    arg.contentType.get() == MediaType.TEXT_PLAIN_TYPE
            }
            val mockResponse = mock<io.micronaut.http.HttpResponse<String>> {
                on { headers } doReturn SimpleHttpHeaders(responseHeaders, DefaultConversionService())
                on { cookies } doReturn SimpleCookies(DefaultConversionService()).apply {
                    put(responseCookie.name, responseCookie)
                }
                on { body() } doReturn responseBody
                on { code() } doReturn responseCode
            }
            given(client.exchange(argThat(httpRequestMatcher), eq(String::class.java)))
                .willReturn(mockResponse)
        }

        val service = JavaScriptAuditingService(HttpClient(client), properties)

        verify("correct response body is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    let response = HttpClient.post(
                        "http://localhost:8080",
                        "$requestBody",
                        {
                            "Content-Type": "text/plain",
                            requestHeader1: "headerValue1",
                            requestHeader2: "headerValue2"
                        }
                    );

                    assertEquals("response.body", "$responseBody", response.body);
                    assertEquals("response.statusCode", $responseCode, response.statusCode);
                    assertEquals("response.headers.size", ${responseHeaders.size}, response.headers.size);

                    let header1List = response.headers.get("header1");

                    assertEquals("header1List.length", 1, header1List.length);
                    assertEquals("header1List.get(0)", "${responseHeaders["header1"]}", header1List.get(0));

                    let header2List = response.headers.get("header2");

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
            val result = service.evaluate(scriptSource)
            assertThat(result).isRightContaining(AuditResult(true))
        }
    }

    @Test
    fun `must throw exception for post() call with invalid headers argument`() {
        val client = mock<BlockingHttpClient>()
        val service = JavaScriptAuditingService(HttpClient(client), properties)
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
                        function() { HttpClient.post("http://localhost:8080", "{\"requestBody\":true}", "headers"); }
                    );

                    return AuditResult.of(true);
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource)
            assertThat(result).isRightContaining(AuditResult(true))
        }
    }

    @Test
    fun `must correctly perform simple request() call`() {
        val client = mock<BlockingHttpClient>()
        val requestUri = URI.create("http://localhost:8080")
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
        val responseBody = "{}"
        val responseCode = 200
        val customMethodName = "NON_STANDARD_METHOD"

        suppose("client will return some HTTP response") {
            val httpRequestMatcher: (HttpRequest<String>) -> Boolean = { arg ->
                arg.uri == requestUri && arg.method == HttpMethod.CUSTOM &&
                    arg.methodName == customMethodName && !arg.body.isPresent && !arg.contentType.isPresent
            }
            val mockResponse = mock<io.micronaut.http.HttpResponse<String>> {
                on { headers } doReturn SimpleHttpHeaders(responseHeaders, DefaultConversionService())
                on { cookies } doReturn SimpleCookies(DefaultConversionService()).apply {
                    put(responseCookie.name, responseCookie)
                }
                on { body() } doReturn responseBody
                on { code() } doReturn responseCode
            }
            given(client.exchange(argThat(httpRequestMatcher), eq(String::class.java)))
                .willReturn(mockResponse)
        }

        val service = JavaScriptAuditingService(HttpClient(client), properties)

        verify("correct response body is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    let response = HttpClient.request("http://localhost:8080", "$customMethodName");

                    assertEquals("response.body", "$responseBody", response.body);
                    assertEquals("response.statusCode", $responseCode, response.statusCode);
                    assertEquals("response.headers.size", ${responseHeaders.size}, response.headers.size);

                    let header1List = response.headers.get("header1");

                    assertEquals("header1List.length", 1, header1List.length);
                    assertEquals("header1List.get(0)", "${responseHeaders["header1"]}", header1List.get(0));

                    let header2List = response.headers.get("header2");

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
            val result = service.evaluate(scriptSource)
            assertThat(result).isRightContaining(AuditResult(true))
        }
    }

    @Test
    fun `must correctly perform request() call with some request body`() {
        val client = mock<BlockingHttpClient>()
        val requestUri = URI.create("http://localhost:8080")
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
        val responseBody = "{}"
        val responseCode = 200
        val customMethodName = "NON_STANDARD_METHOD"
        val requestBody = "exampleRequest"

        suppose("client will return some HTTP response") {
            val httpRequestMatcher: (HttpRequest<String>) -> Boolean = { arg ->
                arg.uri == requestUri && arg.method == HttpMethod.CUSTOM &&
                    arg.methodName == customMethodName && arg.body.get() == requestBody &&
                    arg.contentType.get() == MediaType.APPLICATION_JSON_TYPE
            }
            val mockResponse = mock<io.micronaut.http.HttpResponse<String>> {
                on { headers } doReturn SimpleHttpHeaders(responseHeaders, DefaultConversionService())
                on { cookies } doReturn SimpleCookies(DefaultConversionService()).apply {
                    put(responseCookie.name, responseCookie)
                }
                on { body() } doReturn responseBody
                on { code() } doReturn responseCode
            }
            given(client.exchange(argThat(httpRequestMatcher), eq(String::class.java)))
                .willReturn(mockResponse)
        }

        val service = JavaScriptAuditingService(HttpClient(client), properties)

        verify("correct response body is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    let response = HttpClient.request("http://localhost:8080", "$customMethodName", "$requestBody");

                    assertEquals("response.body", "$responseBody", response.body);
                    assertEquals("response.statusCode", $responseCode, response.statusCode);
                    assertEquals("response.headers.size", ${responseHeaders.size}, response.headers.size);

                    let header1List = response.headers.get("header1");

                    assertEquals("header1List.length", 1, header1List.length);
                    assertEquals("header1List.get(0)", "${responseHeaders["header1"]}", header1List.get(0));

                    let header2List = response.headers.get("header2");

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
            val result = service.evaluate(scriptSource)
            assertThat(result).isRightContaining(AuditResult(true))
        }
    }

    @Test
    fun `must correctly perform request() call with some headers`() {
        val client = mock<BlockingHttpClient>()
        val requestUri = URI.create("http://localhost:8080")
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
        val responseBody = "{}"
        val responseCode = 200
        val customMethodName = "NON_STANDARD_METHOD"
        val requestBody = "exampleRequest"

        suppose("client will return some HTTP response") {
            val httpRequestMatcher: (HttpRequest<String>) -> Boolean = { arg ->
                val headersMap = arg.headers.asMap()
                arg.uri == requestUri && arg.method == HttpMethod.CUSTOM &&
                    arg.methodName == customMethodName && arg.body.get() == requestBody && headersMap.size == 3 &&
                    headersMap["Content-Type"]?.size == 1 && headersMap["Content-Type"]!![0] == "text/plain" &&
                    headersMap["requestHeader1"]?.size == 1 && headersMap["requestHeader1"]!![0] == "headerValue1" &&
                    headersMap["requestHeader2"]?.size == 1 && headersMap["requestHeader2"]!![0] == "headerValue2" &&
                    arg.contentType.get() == MediaType.TEXT_PLAIN_TYPE
            }
            val mockResponse = mock<io.micronaut.http.HttpResponse<String>> {
                on { headers } doReturn SimpleHttpHeaders(responseHeaders, DefaultConversionService())
                on { cookies } doReturn SimpleCookies(DefaultConversionService()).apply {
                    put(responseCookie.name, responseCookie)
                }
                on { body() } doReturn responseBody
                on { code() } doReturn responseCode
            }
            given(client.exchange(argThat(httpRequestMatcher), eq(String::class.java)))
                .willReturn(mockResponse)
        }

        val service = JavaScriptAuditingService(HttpClient(client), properties)

        verify("correct response body is returned") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    let response = HttpClient.request(
                        "http://localhost:8080",
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
                    assertEquals("response.headers.size", ${responseHeaders.size}, response.headers.size);

                    let header1List = response.headers.get("header1");

                    assertEquals("header1List.length", 1, header1List.length);
                    assertEquals("header1List.get(0)", "${responseHeaders["header1"]}", header1List.get(0));

                    let header2List = response.headers.get("header2");

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
            val result = service.evaluate(scriptSource)
            assertThat(result).isRightContaining(AuditResult(true))
        }
    }

    @Test
    fun `must throw exception for request() call with invalid headers argument`() {
        val client = mock<BlockingHttpClient>()
        val service = JavaScriptAuditingService(HttpClient(client), properties)
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
                                "http://localhost:8080",
                                "NON_STANDARD_METHOD",
                                "{\"requestBody\":true}",
                                "headers"
                            );
                        }
                    );

                    return AuditResult.of(true);
                }
            """.trimIndent()
            val result = service.evaluate(scriptSource)
            assertThat(result).isRightContaining(AuditResult(true))
        }
    }
}
