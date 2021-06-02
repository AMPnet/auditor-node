package com.ampnet.auditornode.script.api.classes

import com.ampnet.auditornode.model.error.EvaluationError.InvalidInputValueError
import com.ampnet.auditornode.script.api.model.HttpCookie
import com.ampnet.auditornode.script.api.model.HttpResponse
import com.ampnet.auditornode.script.api.model.ListApi
import com.ampnet.auditornode.script.api.model.MapApi
import com.ampnet.auditornode.util.NativeReflection
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.cookie.SameSite
import org.graalvm.polyglot.HostAccess.Export
import org.graalvm.polyglot.Value
import javax.inject.Singleton

@Singleton
@NativeReflection
class HttpClient(private val blockingHttpClient: BlockingHttpClient) {

    @Export
    @JvmOverloads
    fun get(url: String, headers: Value? = null): HttpResponse {
        return httpRequest(
            headers = headers,
            request = HttpRequest.GET(url),
            methodCall = "HttpClient.get()",
            argumentIndex = 1
        )
    }

    @Export
    @JvmOverloads
    fun post(url: String, body: String = "", headers: Value? = null): HttpResponse {
        return httpRequest(
            headers = headers,
            request = HttpRequest.POST(url, body).apply {
                if (body.isNotEmpty()) {
                    contentType(MediaType.APPLICATION_JSON)
                }
            },
            methodCall = "HttpClient.post()",
            argumentIndex = 2
        )
    }

    @Export
    @JvmOverloads
    fun request(url: String, method: String, body: String = "", headers: Value? = null): HttpResponse {
        val upperCaseMethod = method.toUpperCase()
        val httpMethod = HttpMethod.parse(upperCaseMethod)
        return httpRequest(
            headers = headers,
            request = HttpRequest.create<String>(httpMethod, url, upperCaseMethod).apply {
                if (body.isNotEmpty()) {
                    body(body)
                    contentType(MediaType.APPLICATION_JSON)
                }
            },
            methodCall = "HttpClient.request()",
            argumentIndex = 3
        )
    }

    private fun httpRequest(
        headers: Value?,
        request: MutableHttpRequest<String>,
        methodCall: String,
        argumentIndex: Int
    ): HttpResponse {
        headers?.let {
            if (!headers.hasMembers()) {
                throw InvalidInputValueError(
                    methodCall = methodCall,
                    argumentIndex = argumentIndex,
                    expectedType = "<JavaScript object>",
                    actualType = headers.toString()
                )
            }

            headers.memberKeys
                .map { Pair(it, headers.getMember(it)) }
                .filter { it.second.isString }
                .forEach { request.headers.set(it.first, it.second.asString()) }
        }

        return convertResponse(blockingHttpClient.exchange(request, String::class.java))
    }

    private fun convertResponse(response: io.micronaut.http.HttpResponse<String>): HttpResponse {
        val responseHeaders = response.headers.associate { Pair(it.key, ListApi(it.value)) }
        val responseCookies = response.cookies.all.map {
            HttpCookie(
                name = it.name,
                value = it.value,
                domain = it.domain,
                path = it.path,
                httpOnly = it.isHttpOnly,
                secure = it.isSecure,
                maxAge = it.maxAge,
                sameSite = it.sameSite.orElse(SameSite.Lax).name
            )
        }

        return HttpResponse(
            body = response.body(),
            statusCode = response.code(),
            headers = MapApi(responseHeaders),
            cookies = ListApi(responseCookies)
        )
    }
}
