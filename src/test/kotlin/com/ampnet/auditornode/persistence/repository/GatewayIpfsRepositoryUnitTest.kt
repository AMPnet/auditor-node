package com.ampnet.auditornode.persistence.repository

import assertk.assertThat
import assertk.assertions.isNotNull
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.configuration.properties.IpfsProperties
import com.ampnet.auditornode.isLeftContaining
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.model.error.IpfsError.IpfsEmptyResponseError
import com.ampnet.auditornode.model.error.IpfsError.IpfsHttpError
import com.ampnet.auditornode.model.error.IpfsError.UnsupportedIpfsOperationError
import com.ampnet.auditornode.persistence.model.IpfsBinaryFile
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.IpfsTextFile
import com.ampnet.auditornode.persistence.repository.impl.GatewayIpfsRepository
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.BlockingHttpClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import reactor.core.publisher.Flux

class GatewayIpfsRepositoryUnitTest : TestBase() {

    private val properties = mock<IpfsProperties>()
    private val client = mock<BlockingHttpClient>()
    private val ipfs = GatewayIpfsRepository(
        properties,
        client
    )

    @BeforeEach
    fun beforeEach() {
        reset(client, properties)
    }

    @Test
    fun `fetchTextFile() must return IpfsHttpError when HTTP request fails with an exception`() {
        val exception = RuntimeException("http exception")

        suppose("HTTP client will throw an exception") {
            given(client.retrieve(any<HttpRequest<String>>(), eq(String::class.java)))
                .willThrow(exception)
            given(properties.gatewayUrl)
                .willReturn("http://localhost:8080/{ipfsHash}")
        }

        verify("IpfsHttpError is returned") {
            val result = ipfs.fetchTextFile(IpfsHash("testHash"))
            assertThat(result).isLeftContaining(IpfsHttpError(exception))
        }
    }

    @Test
    fun `fetchTextFile() must return IpfsEmptyResponseError when null response body is returned`() {
        suppose("HTTP client will return a null response") {
            given(client.retrieve(any<HttpRequest<String>>(), eq(String::class.java)))
                .willReturn(null)
            given(properties.gatewayUrl)
                .willReturn("http://localhost:8080/{ipfsHash}")
        }

        verify("IpfsEmptyResponseError is returned") {
            val hash = IpfsHash("testHash")
            val result = ipfs.fetchTextFile(hash)
            assertThat(result).isLeftContaining(IpfsEmptyResponseError(hash))
        }
    }

    @Test
    fun `fetchTextFile() must correctly substitute {ipfsHash} and return a file`() {
        val hash = IpfsHash("testHash")
        val expectedFileUrl = "http://localhost:8080/test-url/${hash.value}/rest"
        val request = HttpRequest.GET<String>(expectedFileUrl)
        val response = "example file data"

        suppose("HTTP client will return a file") {
            val httpRequestMatcher: (HttpRequest<String>) -> Boolean = { arg ->
                arg.uri == request.uri && arg.method == HttpMethod.GET
            }
            given(properties.gatewayUrl)
                .willReturn("http://localhost:8080/test-url/{ipfsHash}/rest")
            given(client.retrieve(argThat(httpRequestMatcher), eq(String::class.java)))
                .willReturn(response)
        }

        verify("correct file data is returned") {
            val result = ipfs.fetchTextFile(hash)
            assertThat(result).isRightContaining(IpfsTextFile(response))
        }
    }

    @Test
    fun `fetchBinaryFile() must return IpfsHttpError when HTTP request fails with an exception`() {
        val exception = RuntimeException("http exception")

        suppose("HTTP client will throw an exception") {
            given(client.retrieve(any<HttpRequest<ByteArray>>(), eq(ByteArray::class.java)))
                .willThrow(exception)
            given(properties.gatewayUrl)
                .willReturn("http://localhost:8080/{ipfsHash}")
        }

        verify("IpfsHttpError is returned") {
            val result = ipfs.fetchBinaryFile(IpfsHash("testHash"))
            assertThat(result).isLeftContaining(IpfsHttpError(exception))
        }
    }

    @Test
    fun `fetchBinaryFile() must return IpfsEmptyResponseError when null response body is returned`() {
        suppose("HTTP client will return a null response") {
            given(client.retrieve(any<HttpRequest<ByteArray>>(), eq(ByteArray::class.java)))
                .willReturn(null)
            given(properties.gatewayUrl)
                .willReturn("http://localhost:8080/{ipfsHash}")
        }

        verify("IpfsEmptyResponseError is returned") {
            val hash = IpfsHash("testHash")
            val result = ipfs.fetchBinaryFile(hash)
            assertThat(result).isLeftContaining(IpfsEmptyResponseError(hash))
        }
    }

    @Test
    fun `fetchBinaryFile() must correctly substitute {ipfsHash} and return a file`() {
        val hash = IpfsHash("testHash")
        val expectedFileUrl = "http://localhost:8080/test-url/${hash.value}/rest"
        val request = HttpRequest.GET<ByteArray>(expectedFileUrl)
        val response = "example file data".toByteArray()

        suppose("HTTP client will return a file") {
            val httpRequestMatcher: (HttpRequest<ByteArray>) -> Boolean = { arg ->
                arg.uri == request.uri && arg.method == HttpMethod.GET
            }
            given(properties.gatewayUrl)
                .willReturn("http://localhost:8080/test-url/{ipfsHash}/rest")
            given(client.retrieve(argThat(httpRequestMatcher), eq(ByteArray::class.java)))
                .willReturn(response)
        }

        verify("correct file data is returned") {
            val result = ipfs.fetchBinaryFile(hash)
            assertThat(result).isRightContaining(IpfsBinaryFile(response))
        }
    }

    @Test
    fun `fetchTextFileFromDirectory() must return IpfsHttpError when HTTP request fails with an exception`() {
        val exception = RuntimeException("http exception")

        suppose("HTTP client will throw an exception") {
            given(client.retrieve(any<HttpRequest<String>>(), eq(String::class.java)))
                .willThrow(exception)
            given(properties.gatewayUrl)
                .willReturn("http://localhost:8080/{ipfsHash}")
        }

        verify("IpfsHttpError is returned") {
            val result = ipfs.fetchTextFileFromDirectory(IpfsHash("testHash"), "example.js")
            assertThat(result).isLeftContaining(IpfsHttpError(exception))
        }
    }

    @Test
    fun `fetchTextFileFromDirectory() must return IpfsEmptyResponseError when null response body is returned`() {
        suppose("HTTP client will return a null response") {
            given(client.retrieve(any<HttpRequest<String>>(), eq(String::class.java)))
                .willReturn(null)
            given(properties.gatewayUrl)
                .willReturn("http://localhost:8080/{ipfsHash}")
        }

        verify("IpfsEmptyResponseError is returned") {
            val hash = IpfsHash("testHash")
            val fileName = "example.js"
            val result = ipfs.fetchTextFileFromDirectory(hash, fileName)
            assertThat(result).isLeftContaining(IpfsEmptyResponseError(hash, fileName))
        }
    }

    @Test
    fun `fetchTextFileFromDirectory() must correctly substitute {ipfsHash} and return a file`() {
        val hash = IpfsHash("testHash")
        val fileName = "example.js"
        val expectedFileUrl = "http://localhost:8080/test-url/${hash.value}/$fileName"
        val request = HttpRequest.GET<String>(expectedFileUrl)
        val response = "example file data"

        suppose("HTTP client will return a file") {
            val httpRequestMatcher: (HttpRequest<String>) -> Boolean = { arg ->
                arg.uri == request.uri && arg.method == HttpMethod.GET
            }
            given(properties.gatewayUrl)
                .willReturn("http://localhost:8080/test-url/{ipfsHash}/")
            given(client.retrieve(argThat(httpRequestMatcher), eq(String::class.java)))
                .willReturn(response)
        }

        verify("correct file data is returned") {
            val result = ipfs.fetchTextFileFromDirectory(hash, fileName)
            assertThat(result).isRightContaining(IpfsTextFile(response))
        }
    }

    @Test
    fun `fetchBinaryFileFromDirectory() must return IpfsHttpError when HTTP request fails with an exception`() {
        val exception = RuntimeException("http exception")

        suppose("HTTP client will throw an exception") {
            given(client.retrieve(any<HttpRequest<ByteArray>>(), eq(ByteArray::class.java)))
                .willThrow(exception)
            given(properties.gatewayUrl)
                .willReturn("http://localhost:8080/{ipfsHash}")
        }

        verify("IpfsHttpError is returned") {
            val result = ipfs.fetchBinaryFileFromDirectory(IpfsHash("testHash"), "example.js")
            assertThat(result).isLeftContaining(IpfsHttpError(exception))
        }
    }

    @Test
    fun `fetchBinaryFileFromDirectory() must return IpfsEmptyResponseError when null response body is returned`() {
        suppose("HTTP client will return a null response") {
            given(client.retrieve(any<HttpRequest<ByteArray>>(), eq(ByteArray::class.java)))
                .willReturn(null)
            given(properties.gatewayUrl)
                .willReturn("http://localhost:8080/{ipfsHash}")
        }

        verify("IpfsEmptyResponseError is returned") {
            val hash = IpfsHash("testHash")
            val fileName = "example.js"
            val result = ipfs.fetchBinaryFileFromDirectory(hash, fileName)
            assertThat(result).isLeftContaining(IpfsEmptyResponseError(hash, fileName))
        }
    }

    @Test
    fun `fetchBinaryFileFromDirectory() must correctly substitute {ipfsHash} and return a file`() {
        val hash = IpfsHash("testHash")
        val fileName = "example.js"
        val expectedFileUrl = "http://localhost:8080/test-url/${hash.value}/$fileName"
        val request = HttpRequest.GET<ByteArray>(expectedFileUrl)
        val response = "example file data".toByteArray()

        suppose("HTTP client will return a file") {
            val httpRequestMatcher: (HttpRequest<ByteArray>) -> Boolean = { arg ->
                arg.uri == request.uri && arg.method == HttpMethod.GET
            }
            given(properties.gatewayUrl)
                .willReturn("http://localhost:8080/test-url/{ipfsHash}/")
            given(client.retrieve(argThat(httpRequestMatcher), eq(ByteArray::class.java)))
                .willReturn(response)
        }

        verify("correct file data is returned") {
            val result = ipfs.fetchBinaryFileFromDirectory(hash, fileName)
            assertThat(result).isRightContaining(IpfsBinaryFile(response))
        }
    }

    @Test
    fun `uploadFilesToDirectory() must return UnsupportedIpfsOperationError`() {
        verify("UnsupportedIpfsOperationError is returned") {
            val result = ipfs.uploadFilesToDirectory(Flux.empty()).block()
            assertThat(result)
                .isNotNull()
                .isLeftContaining(UnsupportedIpfsOperationError)
        }
    }
}
