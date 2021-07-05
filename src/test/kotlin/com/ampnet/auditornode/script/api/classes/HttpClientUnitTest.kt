package com.ampnet.auditornode.script.api.classes

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.model.error.EvaluationError.InvalidInputValueError
import com.ampnet.auditornode.script.api.model.HttpCookie
import com.ampnet.auditornode.script.api.model.ListApi
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.cookie.SameSite
import io.micronaut.http.simple.SimpleHttpHeaders
import io.micronaut.http.simple.cookies.SimpleCookie
import io.micronaut.http.simple.cookies.SimpleCookies
import org.graalvm.polyglot.Value
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import java.util.Optional

class HttpClientUnitTest : TestBase() {

    private val blockingHttpClient = mock<BlockingHttpClient>()
    private val service = HttpClient(blockingHttpClient)

    private val <T> Optional<T>.isEmpty
        get() = isPresent.not()

    @BeforeEach
    fun beforeEach() {
        reset(blockingHttpClient)
    }

    @Test
    fun `must correctly send get request without headers`() {
        val responseHeaders = SimpleHttpHeaders(mock()).apply {
            set("test", "value")
        }
        val cookie = SimpleCookie("example", "cookieValue").apply {
            domain("domain")
            path("path")
            httpOnly(true)
            secure(true)
            maxAge(123L)
            sameSite(SameSite.Strict)
        }
        val responseCookies = SimpleCookies(mock()).apply {
            put(cookie.name, cookie)
        }
        val responseBody = "test response"
        val responseCode = 200
        val response = mock<io.micronaut.http.HttpResponse<String>> {
            on { headers } doReturn responseHeaders
            on { cookies } doReturn responseCookies
            on { body() } doReturn responseBody
            on { code() } doReturn responseCode
        }
        val uri = "http://localhost:8080/test"

        suppose("some HTTP response is returned") {
            val httpRequestMatcher: (HttpRequest<String>) -> Boolean = { arg ->
                arg.uri.toString() == uri && arg.method == HttpMethod.GET
            }
            given(blockingHttpClient.exchange(argThat(httpRequestMatcher), eq(String::class.java)))
                .willReturn(response)
        }

        verify("response is correctly read") {
            val result = service.get(uri)

            assertThat(result.body)
                .isEqualTo(responseBody)
            assertThat(result.statusCode)
                .isEqualTo(responseCode)
            assertThat(result.headers.size)
                .isEqualTo(1)
            assertThat(result.headers.keys())
                .isEqualTo(ListApi(listOf("test")))
            assertThat(response.headers["test"])
                .isEqualTo("value")
            assertThat(result.cookies.length)
                .isEqualTo(1)
            assertThat(result.cookies[0])
                .isEqualTo(
                    HttpCookie(
                        name = cookie.name,
                        value = cookie.value,
                        domain = cookie.domain,
                        path = cookie.path,
                        httpOnly = cookie.isHttpOnly,
                        secure = cookie.isSecure,
                        maxAge = cookie.maxAge,
                        sameSite = cookie.sameSite.get().toString()
                    )
                )
        }
    }

    @Test
    fun `must correctly send get request with some headers`() {
        val responseHeaders = SimpleHttpHeaders(mock()).apply {
            set("test", "value")
        }
        val cookie = SimpleCookie("example", "cookieValue").apply {
            domain("domain")
            path("path")
            httpOnly(true)
            secure(true)
            maxAge(123L)
            sameSite(SameSite.Strict)
        }
        val responseCookies = SimpleCookies(mock()).apply {
            put(cookie.name, cookie)
        }
        val responseBody = "test response"
        val responseCode = 200
        val response = mock<io.micronaut.http.HttpResponse<String>> {
            on { headers } doReturn responseHeaders
            on { cookies } doReturn responseCookies
            on { body() } doReturn responseBody
            on { code() } doReturn responseCode
        }
        val uri = "http://localhost:8080/test"

        suppose("some HTTP response is returned") {
            val httpRequestMatcher: (HttpRequest<String>) -> Boolean = { arg ->
                arg.uri.toString() == uri && arg.method == HttpMethod.GET &&
                    arg.headers.get("test-header") == "test-value"
            }
            given(blockingHttpClient.exchange(argThat(httpRequestMatcher), eq(String::class.java)))
                .willReturn(response)
        }

        verify("response is correctly read") {
            val mockValue = mock<Value> {
                on { isString } doReturn true
                on { asString() } doReturn "test-value"
            }
            val mockHeaders = mock<Value> {
                on { hasMembers() } doReturn true
                on { memberKeys } doReturn setOf("test-header")
                on { getMember("test-header") } doReturn mockValue
                on { toString() } doReturn "{}"
            }

            val result = service.get(url = uri, headers = mockHeaders)

            assertThat(result.body)
                .isEqualTo(responseBody)
            assertThat(result.statusCode)
                .isEqualTo(responseCode)
            assertThat(result.headers.size)
                .isEqualTo(1)
            assertThat(result.headers.keys())
                .isEqualTo(ListApi(listOf("test")))
            assertThat(response.headers["test"])
                .isEqualTo("value")
            assertThat(result.cookies.length)
                .isEqualTo(1)
            assertThat(result.cookies[0])
                .isEqualTo(
                    HttpCookie(
                        name = cookie.name,
                        value = cookie.value,
                        domain = cookie.domain,
                        path = cookie.path,
                        httpOnly = cookie.isHttpOnly,
                        secure = cookie.isSecure,
                        maxAge = cookie.maxAge,
                        sameSite = cookie.sameSite.get().toString()
                    )
                )
        }
    }

    @Test
    fun `must throw InvalidInputValueError for get request with invalid headers`() {
        verify("InvalidInputValueError is thrown") {
            val mockHeaders = mock<Value> {
                on { hasMembers() } doReturn false
                on { toString() } doReturn "{}"
            }

            val thrownException = assertThrows<InvalidInputValueError> {
                service.get(
                    url = "testUrl",
                    headers = mockHeaders
                )
            }

            assertThat(thrownException.methodCall)
                .isEqualTo("HttpClient.get()")
            assertThat(thrownException.argumentIndex)
                .isEqualTo(1)
            assertThat(thrownException.expectedType)
                .isEqualTo("<JavaScript object>")
            assertThat(thrownException.actualType)
                .isEqualTo("{}")
        }
    }

    @Test
    fun `must correctly send post request without body and headers`() {
        val responseHeaders = SimpleHttpHeaders(mock()).apply {
            set("test", "value")
        }
        val cookie = SimpleCookie("example", "cookieValue").apply {
            domain("domain")
            path("path")
            httpOnly(true)
            secure(true)
            maxAge(123L)
            sameSite(SameSite.Strict)
        }
        val responseCookies = SimpleCookies(mock()).apply {
            put(cookie.name, cookie)
        }
        val responseBody = "test response"
        val responseCode = 200
        val response = mock<io.micronaut.http.HttpResponse<String>> {
            on { headers } doReturn responseHeaders
            on { cookies } doReturn responseCookies
            on { body() } doReturn responseBody
            on { code() } doReturn responseCode
        }
        val uri = "http://localhost:8080/test"

        suppose("some HTTP response is returned") {
            val httpRequestMatcher: (HttpRequest<String>) -> Boolean = { arg ->
                arg.uri.toString() == uri && arg.method == HttpMethod.POST && arg.body == Optional.of("") &&
                    arg.contentType.isEmpty
            }
            given(blockingHttpClient.exchange(argThat(httpRequestMatcher), eq(String::class.java)))
                .willReturn(response)
        }

        verify("response is correctly read") {
            val result = service.post(uri)

            assertThat(result.body)
                .isEqualTo(responseBody)
            assertThat(result.statusCode)
                .isEqualTo(responseCode)
            assertThat(result.headers.size)
                .isEqualTo(1)
            assertThat(result.headers.keys())
                .isEqualTo(ListApi(listOf("test")))
            assertThat(response.headers["test"])
                .isEqualTo("value")
            assertThat(result.cookies.length)
                .isEqualTo(1)
            assertThat(result.cookies[0])
                .isEqualTo(
                    HttpCookie(
                        name = cookie.name,
                        value = cookie.value,
                        domain = cookie.domain,
                        path = cookie.path,
                        httpOnly = cookie.isHttpOnly,
                        secure = cookie.isSecure,
                        maxAge = cookie.maxAge,
                        sameSite = cookie.sameSite.get().toString()
                    )
                )
        }
    }

    @Test
    fun `must correctly send post request with some body and no headers`() {
        val responseHeaders = SimpleHttpHeaders(mock()).apply {
            set("test", "value")
        }
        val cookie = SimpleCookie("example", "cookieValue").apply {
            domain("domain")
            path("path")
            httpOnly(true)
            secure(true)
            maxAge(123L)
            sameSite(SameSite.Strict)
        }
        val responseCookies = SimpleCookies(mock()).apply {
            put(cookie.name, cookie)
        }
        val responseBody = "test response"
        val responseCode = 200
        val response = mock<io.micronaut.http.HttpResponse<String>> {
            on { headers } doReturn responseHeaders
            on { cookies } doReturn responseCookies
            on { body() } doReturn responseBody
            on { code() } doReturn responseCode
        }
        val uri = "http://localhost:8080/test"

        suppose("some HTTP response is returned") {
            val httpRequestMatcher: (HttpRequest<String>) -> Boolean = { arg ->
                arg.uri.toString() == uri && arg.method == HttpMethod.POST && arg.body == Optional.of("{}") &&
                    arg.contentType == Optional.of(MediaType.APPLICATION_JSON_TYPE)
            }
            given(blockingHttpClient.exchange(argThat(httpRequestMatcher), eq(String::class.java)))
                .willReturn(response)
        }

        verify("response is correctly read") {
            val result = service.post(url = uri, body = "{}")

            assertThat(result.body)
                .isEqualTo(responseBody)
            assertThat(result.statusCode)
                .isEqualTo(responseCode)
            assertThat(result.headers.size)
                .isEqualTo(1)
            assertThat(result.headers.keys())
                .isEqualTo(ListApi(listOf("test")))
            assertThat(response.headers["test"])
                .isEqualTo("value")
            assertThat(result.cookies.length)
                .isEqualTo(1)
            assertThat(result.cookies[0])
                .isEqualTo(
                    HttpCookie(
                        name = cookie.name,
                        value = cookie.value,
                        domain = cookie.domain,
                        path = cookie.path,
                        httpOnly = cookie.isHttpOnly,
                        secure = cookie.isSecure,
                        maxAge = cookie.maxAge,
                        sameSite = cookie.sameSite.get().toString()
                    )
                )
        }
    }

    @Test
    fun `must correctly send post request with some body and some headers`() {
        val responseHeaders = SimpleHttpHeaders(mock()).apply {
            set("test", "value")
        }
        val cookie = SimpleCookie("example", "cookieValue").apply {
            domain("domain")
            path("path")
            httpOnly(true)
            secure(true)
            maxAge(123L)
            sameSite(SameSite.Strict)
        }
        val responseCookies = SimpleCookies(mock()).apply {
            put(cookie.name, cookie)
        }
        val responseBody = "test response"
        val responseCode = 200
        val response = mock<io.micronaut.http.HttpResponse<String>> {
            on { headers } doReturn responseHeaders
            on { cookies } doReturn responseCookies
            on { body() } doReturn responseBody
            on { code() } doReturn responseCode
        }
        val uri = "http://localhost:8080/test"

        suppose("some HTTP response is returned") {
            val httpRequestMatcher: (HttpRequest<String>) -> Boolean = { arg ->
                arg.uri.toString() == uri && arg.method == HttpMethod.POST && arg.body == Optional.of("{}") &&
                    arg.contentType == Optional.of(MediaType.TEXT_PLAIN_TYPE) &&
                    arg.headers.get("test-header") == "test-value"
            }
            given(blockingHttpClient.exchange(argThat(httpRequestMatcher), eq(String::class.java)))
                .willReturn(response)
        }

        verify("response is correctly read") {
            val mockValue1 = mock<Value> {
                on { isString } doReturn true
                on { asString() } doReturn "test-value"
            }
            val mockValue2 = mock<Value> {
                on { isString } doReturn true
                on { asString() } doReturn MediaType.TEXT_PLAIN
            }
            val mockHeaders = mock<Value> {
                on { hasMembers() } doReturn true
                on { memberKeys } doReturn setOf("test-header", "Content-Type")
                on { getMember("test-header") } doReturn mockValue1
                on { getMember("Content-Type") } doReturn mockValue2
                on { toString() } doReturn "{}"
            }

            val result = service.post(url = uri, body = "{}", headers = mockHeaders)

            assertThat(result.body)
                .isEqualTo(responseBody)
            assertThat(result.statusCode)
                .isEqualTo(responseCode)
            assertThat(result.headers.size)
                .isEqualTo(1)
            assertThat(result.headers.keys())
                .isEqualTo(ListApi(listOf("test")))
            assertThat(response.headers["test"])
                .isEqualTo("value")
            assertThat(result.cookies.length)
                .isEqualTo(1)
            assertThat(result.cookies[0])
                .isEqualTo(
                    HttpCookie(
                        name = cookie.name,
                        value = cookie.value,
                        domain = cookie.domain,
                        path = cookie.path,
                        httpOnly = cookie.isHttpOnly,
                        secure = cookie.isSecure,
                        maxAge = cookie.maxAge,
                        sameSite = cookie.sameSite.get().toString()
                    )
                )
        }
    }

    @Test
    fun `must throw InvalidInputValueError for post request with invalid headers`() {
        verify("InvalidInputValueError is thrown") {
            val mockHeaders = mock<Value> {
                on { hasMembers() } doReturn false
                on { toString() } doReturn "{}"
            }

            val thrownException = assertThrows<InvalidInputValueError> {
                service.post(
                    url = "testUrl",
                    body = "",
                    headers = mockHeaders
                )
            }

            assertThat(thrownException.methodCall)
                .isEqualTo("HttpClient.post()")
            assertThat(thrownException.argumentIndex)
                .isEqualTo(2)
            assertThat(thrownException.expectedType)
                .isEqualTo("<JavaScript object>")
            assertThat(thrownException.actualType)
                .isEqualTo("{}")
        }
    }

    @Test
    fun `must correctly send HTTP request without body and headers`() {
        val responseHeaders = SimpleHttpHeaders(mock()).apply {
            set("test", "value")
        }
        val cookie = SimpleCookie("example", "cookieValue").apply {
            domain("domain")
            path("path")
            httpOnly(true)
            secure(true)
            maxAge(123L)
            sameSite(SameSite.Strict)
        }
        val responseCookies = SimpleCookies(mock()).apply {
            put(cookie.name, cookie)
        }
        val responseBody = "test response"
        val responseCode = 200
        val response = mock<io.micronaut.http.HttpResponse<String>> {
            on { headers } doReturn responseHeaders
            on { cookies } doReturn responseCookies
            on { body() } doReturn responseBody
            on { code() } doReturn responseCode
        }
        val uri = "http://localhost:8080/test"

        suppose("some HTTP response is returned") {
            val httpRequestMatcher: (HttpRequest<String>) -> Boolean = { arg ->
                arg.uri.toString() == uri && arg.method == HttpMethod.CUSTOM && arg.methodName == "NON_STANDARD" &&
                    arg.body.isEmpty && arg.contentType.isEmpty
            }
            given(blockingHttpClient.exchange(argThat(httpRequestMatcher), eq(String::class.java)))
                .willReturn(response)
        }

        verify("response is correctly read") {
            val result = service.request(url = uri, method = "NON_STANDARD")

            assertThat(result.body)
                .isEqualTo(responseBody)
            assertThat(result.statusCode)
                .isEqualTo(responseCode)
            assertThat(result.headers.size)
                .isEqualTo(1)
            assertThat(result.headers.keys())
                .isEqualTo(ListApi(listOf("test")))
            assertThat(response.headers["test"])
                .isEqualTo("value")
            assertThat(result.cookies.length)
                .isEqualTo(1)
            assertThat(result.cookies[0])
                .isEqualTo(
                    HttpCookie(
                        name = cookie.name,
                        value = cookie.value,
                        domain = cookie.domain,
                        path = cookie.path,
                        httpOnly = cookie.isHttpOnly,
                        secure = cookie.isSecure,
                        maxAge = cookie.maxAge,
                        sameSite = cookie.sameSite.get().toString()
                    )
                )
        }
    }

    @Test
    fun `must correctly send HTTP request with some body and no headers`() {
        val responseHeaders = SimpleHttpHeaders(mock()).apply {
            set("test", "value")
        }
        val cookie = SimpleCookie("example", "cookieValue").apply {
            domain("domain")
            path("path")
            httpOnly(true)
            secure(true)
            maxAge(123L)
            sameSite(SameSite.Strict)
        }
        val responseCookies = SimpleCookies(mock()).apply {
            put(cookie.name, cookie)
        }
        val responseBody = "test response"
        val responseCode = 200
        val response = mock<io.micronaut.http.HttpResponse<String>> {
            on { headers } doReturn responseHeaders
            on { cookies } doReturn responseCookies
            on { body() } doReturn responseBody
            on { code() } doReturn responseCode
        }
        val uri = "http://localhost:8080/test"

        suppose("some HTTP response is returned") {
            val httpRequestMatcher: (HttpRequest<String>) -> Boolean = { arg ->
                arg.uri.toString() == uri && arg.method == HttpMethod.CUSTOM && arg.methodName == "NON_STANDARD" &&
                    arg.body == Optional.of("{}") && arg.contentType == Optional.of(MediaType.APPLICATION_JSON_TYPE)
            }
            given(blockingHttpClient.exchange(argThat(httpRequestMatcher), eq(String::class.java)))
                .willReturn(response)
        }

        verify("response is correctly read") {
            val result = service.request(url = uri, method = "NON_STANDARD", body = "{}")

            assertThat(result.body)
                .isEqualTo(responseBody)
            assertThat(result.statusCode)
                .isEqualTo(responseCode)
            assertThat(result.headers.size)
                .isEqualTo(1)
            assertThat(result.headers.keys())
                .isEqualTo(ListApi(listOf("test")))
            assertThat(response.headers["test"])
                .isEqualTo("value")
            assertThat(result.cookies.length)
                .isEqualTo(1)
            assertThat(result.cookies[0])
                .isEqualTo(
                    HttpCookie(
                        name = cookie.name,
                        value = cookie.value,
                        domain = cookie.domain,
                        path = cookie.path,
                        httpOnly = cookie.isHttpOnly,
                        secure = cookie.isSecure,
                        maxAge = cookie.maxAge,
                        sameSite = cookie.sameSite.get().toString()
                    )
                )
        }
    }

    @Test
    fun `must correctly send HTTP request with some body and some headers`() {
        val responseHeaders = SimpleHttpHeaders(mock()).apply {
            set("test", "value")
        }
        val cookie = SimpleCookie("example", "cookieValue").apply {
            domain("domain")
            path("path")
            httpOnly(true)
            secure(true)
            maxAge(123L)
            sameSite(SameSite.Strict)
        }
        val responseCookies = SimpleCookies(mock()).apply {
            put(cookie.name, cookie)
        }
        val responseBody = "test response"
        val responseCode = 200
        val response = mock<io.micronaut.http.HttpResponse<String>> {
            on { headers } doReturn responseHeaders
            on { cookies } doReturn responseCookies
            on { body() } doReturn responseBody
            on { code() } doReturn responseCode
        }
        val uri = "http://localhost:8080/test"

        suppose("some HTTP response is returned") {
            val httpRequestMatcher: (HttpRequest<String>) -> Boolean = { arg ->
                arg.uri.toString() == uri && arg.method == HttpMethod.CUSTOM && arg.methodName == "NON_STANDARD" &&
                    arg.body == Optional.of("{}") && arg.contentType == Optional.of(MediaType.TEXT_PLAIN_TYPE) &&
                    arg.headers.get("test-header") == "test-value"
            }
            given(blockingHttpClient.exchange(argThat(httpRequestMatcher), eq(String::class.java)))
                .willReturn(response)
        }

        verify("response is correctly read") {
            val mockValue1 = mock<Value> {
                on { isString } doReturn true
                on { asString() } doReturn "test-value"
            }
            val mockValue2 = mock<Value> {
                on { isString } doReturn true
                on { asString() } doReturn MediaType.TEXT_PLAIN
            }
            val mockHeaders = mock<Value> {
                on { hasMembers() } doReturn true
                on { memberKeys } doReturn setOf("test-header", "Content-Type")
                on { getMember("test-header") } doReturn mockValue1
                on { getMember("Content-Type") } doReturn mockValue2
                on { toString() } doReturn "{}"
            }

            val result = service.request(url = uri, method = "NON_STANDARD", body = "{}", headers = mockHeaders)

            assertThat(result.body)
                .isEqualTo(responseBody)
            assertThat(result.statusCode)
                .isEqualTo(responseCode)
            assertThat(result.headers.size)
                .isEqualTo(1)
            assertThat(result.headers.keys())
                .isEqualTo(ListApi(listOf("test")))
            assertThat(response.headers["test"])
                .isEqualTo("value")
            assertThat(result.cookies.length)
                .isEqualTo(1)
            assertThat(result.cookies[0])
                .isEqualTo(
                    HttpCookie(
                        name = cookie.name,
                        value = cookie.value,
                        domain = cookie.domain,
                        path = cookie.path,
                        httpOnly = cookie.isHttpOnly,
                        secure = cookie.isSecure,
                        maxAge = cookie.maxAge,
                        sameSite = cookie.sameSite.get().toString()
                    )
                )
        }
    }

    @Test
    fun `must throw InvalidInputValueError for HTTP request with invalid headers`() {
        verify("InvalidInputValueError is thrown") {
            val mockHeaders = mock<Value> {
                on { hasMembers() } doReturn false
                on { toString() } doReturn "{}"
            }

            val thrownException = assertThrows<InvalidInputValueError> {
                service.request(
                    url = "testUrl",
                    method = "PATCH",
                    body = "",
                    headers = mockHeaders
                )
            }

            assertThat(thrownException.methodCall)
                .isEqualTo("HttpClient.request()")
            assertThat(thrownException.argumentIndex)
                .isEqualTo(3)
            assertThat(thrownException.expectedType)
                .isEqualTo("<JavaScript object>")
            assertThat(thrownException.actualType)
                .isEqualTo("{}")
        }
    }
}
