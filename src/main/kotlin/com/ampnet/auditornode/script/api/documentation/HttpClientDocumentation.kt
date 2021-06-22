package com.ampnet.auditornode.script.api.documentation

import com.ampnet.auditornode.script.api.model.HttpResponse
import com.amptnet.auditornode.documentation.annotation.ScriptApi
import com.amptnet.auditornode.documentation.annotation.ScriptApiCategory
import com.amptnet.auditornode.documentation.annotation.ScriptFunction
import org.graalvm.polyglot.Value

@ScriptApi(
    description = "Provides support for making blocking HTTP calls from the auditing scripts. Request and response " +
        "bodies are always of `String` type, and the default request content type is `application/json` if request " +
        "body is provided. This content type can be changed by specifying the `Content-Type` header value.",
    category = ScriptApiCategory.API,
    hasStaticApi = true,
    apiObjectName = "HttpClient"
)
interface HttpClientDocumentation {

    @ScriptFunction(
        description = "Sends a `GET` request to the specified URL and returns the response.",
        exampleCall = "`{apiObjectName}.get(\"http://example.com/\");`"
    )
    fun get(url: String): HttpResponse

    @ScriptFunction(
        description = "Sends a `GET` request with provided headers to the specified URL and returns the response. " +
            "The headers object should consist of key-value pairs which are of `String` type.",
        exampleCall = "`{apiObjectName}.get(\"http://example.com/\", { \"Accept\": \"application/json\" });`"
    )
    fun get(url: String, headers: Value?): HttpResponse

    @ScriptFunction(
        description = "Sends a `POST` request with empty request body to the specified URL and returns the response.",
        exampleCall = "`{apiObjectName}.post(\"http://example.com/\");`"
    )
    fun post(url: String): HttpResponse

    @ScriptFunction(
        description = "Sends a `POST` request with provided request body to the specified URL and returns the " +
            "response.",
        exampleCall = "`{apiObjectName}.post(\"http://example.com/\", \"exampleRequestBody\");`"
    )
    fun post(url: String, body: String): HttpResponse

    @ScriptFunction(
        description = "Sends a `POST` request with provided request body and headers to the specified URL and " +
            "returns the response. The headers object should consist of key-value pairs which are of `String` type.",
        exampleCall = "`{apiObjectName}.post(\"http://example.com/\", \"exampleRequestBody\", " +
            "{ \"Accept\": \"application/json\" });`"
    )
    fun post(url: String, body: String, headers: Value?): HttpResponse

    @ScriptFunction(
        description = "Sends a request with specified HTTP method to the specified URL and returns the response.",
        exampleCall = "`{apiObjectName}.request(\"http://example.com/\", \"CUSTOM_METHOD\");`"
    )
    fun request(url: String, method: String): HttpResponse

    @ScriptFunction(
        description = "Sends a request with specified HTTP method and request body to the specified URL and returns " +
            "the response.",
        exampleCall = "`{apiObjectName}.request(\"http://example.com/\", \"CUSTOM_METHOD\", \"exampleRequestBody\");`"
    )
    fun request(url: String, method: String, body: String): HttpResponse

    @ScriptFunction(
        description = "Sends a request with specified HTTP method, request body and headers to the specified URL and " +
            "returns the response. The headers object should consist of key-value pairs which are of `String` type.",
        exampleCall = "`{apiObjectName}.request(\"http://example.com/\", \"CUSTOM_METHOD\", \"exampleRequestBody\", " +
            "{ \"Accept\": \"application/json\" });`"
    )
    fun request(url: String, method: String, body: String, headers: Value?): HttpResponse
}
