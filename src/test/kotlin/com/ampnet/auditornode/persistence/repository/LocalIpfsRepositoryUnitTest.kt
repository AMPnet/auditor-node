package com.ampnet.auditornode.persistence.repository

import assertk.assertThat
import assertk.assertions.isNotNull
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.UnitTestUtils
import com.ampnet.auditornode.configuration.properties.IpfsProperties
import com.ampnet.auditornode.isLeftContaining
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.model.error.IpfsError.IpfsEmptyResponseError
import com.ampnet.auditornode.model.error.IpfsError.IpfsHttpError
import com.ampnet.auditornode.model.error.IpfsError.MissingUploadedIpfsDirectoryHash
import com.ampnet.auditornode.model.response.IpfsDirectoryUploadResponse
import com.ampnet.auditornode.model.response.IpfsFileUploadResponse
import com.ampnet.auditornode.persistence.model.IpfsBinaryFile
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.IpfsTextFile
import com.ampnet.auditornode.persistence.model.NamedIpfsFile
import com.ampnet.auditornode.persistence.repository.impl.LocalIpfsRepository
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.RxHttpClient
import io.reactivex.Flowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import reactor.core.publisher.Flux
import java.util.Optional

class LocalIpfsRepositoryUnitTest : TestBase() {

    private val properties = mock<IpfsProperties>()
    private val blockingClient = mock<BlockingHttpClient>()
    private val reactiveClient = mock<RxHttpClient>()
    private val ipfs = LocalIpfsRepository(
        properties,
        blockingClient,
        reactiveClient,
        UnitTestUtils.objectMapper
    )

    @BeforeEach
    fun beforeEach() {
        reset(blockingClient, properties)
    }

    @Test
    fun `fetchTextFile() must return IpfsHttpError when HTTP request fails with an exception`() {
        val exception = RuntimeException("http exception")

        suppose("HTTP client will throw an exception") {
            given(blockingClient.retrieve(any<HttpRequest<String>>(), eq(String::class.java)))
                .willThrow(exception)
            given(properties.localClientPort)
                .willReturn(5001)
        }

        verify("IpfsHttpError is returned") {
            val result = ipfs.fetchTextFile(IpfsHash("testHash"))
            assertThat(result).isLeftContaining(IpfsHttpError(exception))
        }
    }

    @Test
    fun `fetchTextFile() must return IpfsEmptyResponseError when null response body is returned`() {
        suppose("HTTP client will return a null response") {
            given(blockingClient.retrieve(any<HttpRequest<String>>(), eq(String::class.java)))
                .willReturn(null)
            given(properties.localClientPort)
                .willReturn(5001)
        }

        verify("IpfsEmptyResponseError is returned") {
            val hash = IpfsHash("testHash")
            val result = ipfs.fetchTextFile(hash)
            assertThat(result).isLeftContaining(IpfsEmptyResponseError(hash))
        }
    }

    @Test
    fun `fetchTextFile() must correctly substitute {ipfsHash}, use provided port and return a file`() {
        val testPort = 1234
        val hash = IpfsHash("testHash")
        val expectedFileUrl = "http://localhost:$testPort/api/v0/cat?arg=${hash.value}"
        val request = HttpRequest.POST(expectedFileUrl, "")
        val response = "example file data"

        suppose("HTTP client will return a file") {
            val httpRequestMatcher: (HttpRequest<String>) -> Boolean = { arg ->
                arg.uri == request.uri && arg.method == HttpMethod.POST && arg.body == Optional.of("")
            }
            given(properties.localClientPort)
                .willReturn(testPort)
            given(blockingClient.retrieve(argThat(httpRequestMatcher), eq(String::class.java)))
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
            given(blockingClient.retrieve(any<HttpRequest<ByteArray>>(), eq(ByteArray::class.java)))
                .willThrow(exception)
            given(properties.localClientPort)
                .willReturn(5001)
        }

        verify("IpfsHttpError is returned") {
            val result = ipfs.fetchBinaryFile(IpfsHash("testHash"))
            assertThat(result).isLeftContaining(IpfsHttpError(exception))
        }
    }

    @Test
    fun `fetchBinaryFile() must return IpfsEmptyResponseError when null response body is returned`() {
        suppose("HTTP client will return a null response") {
            given(blockingClient.retrieve(any<HttpRequest<ByteArray>>(), eq(ByteArray::class.java)))
                .willReturn(null)
            given(properties.localClientPort)
                .willReturn(5001)
        }

        verify("IpfsEmptyResponseError is returned") {
            val hash = IpfsHash("testHash")
            val result = ipfs.fetchBinaryFile(hash)
            assertThat(result).isLeftContaining(IpfsEmptyResponseError(hash))
        }
    }

    @Test
    fun `fetchBinaryFile() must correctly substitute {ipfsHash}, use provided port and return a file`() {
        val testPort = 1234
        val hash = IpfsHash("testHash")
        val expectedFileUrl = "http://localhost:$testPort/api/v0/cat?arg=${hash.value}"
        val request = HttpRequest.POST(expectedFileUrl, "")
        val response = "example file data".toByteArray()

        suppose("HTTP client will return a file") {
            val httpRequestMatcher: (HttpRequest<ByteArray>) -> Boolean = { arg ->
                arg.uri == request.uri && arg.method == HttpMethod.POST && arg.body == Optional.of("")
            }
            given(properties.localClientPort)
                .willReturn(testPort)
            given(blockingClient.retrieve(argThat(httpRequestMatcher), eq(ByteArray::class.java)))
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
            given(blockingClient.retrieve(any<HttpRequest<String>>(), eq(String::class.java)))
                .willThrow(exception)
            given(properties.localClientPort)
                .willReturn(5001)
        }

        verify("IpfsHttpError is returned") {
            val result = ipfs.fetchTextFileFromDirectory(IpfsHash("testHash"), "example.js")
            assertThat(result).isLeftContaining(IpfsHttpError(exception))
        }
    }

    @Test
    fun `fetchTextFileFromDirectory() must return IpfsEmptyResponseError when null response body is returned`() {
        suppose("HTTP client will return a null response") {
            given(blockingClient.retrieve(any<HttpRequest<String>>(), eq(String::class.java)))
                .willReturn(null)
            given(properties.localClientPort)
                .willReturn(5001)
        }

        verify("IpfsEmptyResponseError is returned") {
            val hash = IpfsHash("testHash")
            val fileName = "example.js"
            val result = ipfs.fetchTextFileFromDirectory(hash, fileName)
            assertThat(result).isLeftContaining(IpfsEmptyResponseError(hash, fileName))
        }
    }

    @Test
    fun `fetchTextFileFromDirectory() must correctly substitute {ipfsHash}, use provided port and return a file`() {
        val testPort = 1234
        val hash = IpfsHash("testHash")
        val fileName = "example.js"
        val expectedFileUrl = "http://localhost:$testPort/api/v0/cat?arg=${hash.value}/$fileName"
        val request = HttpRequest.POST(expectedFileUrl, "")
        val response = "example file data"

        suppose("HTTP client will return a file") {
            val httpRequestMatcher: (HttpRequest<String>) -> Boolean = { arg ->
                arg.uri == request.uri && arg.method == HttpMethod.POST && arg.body == Optional.of("")
            }
            given(properties.localClientPort)
                .willReturn(testPort)
            given(blockingClient.retrieve(argThat(httpRequestMatcher), eq(String::class.java)))
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
            given(blockingClient.retrieve(any<HttpRequest<ByteArray>>(), eq(ByteArray::class.java)))
                .willThrow(exception)
            given(properties.localClientPort)
                .willReturn(5001)
        }

        verify("IpfsHttpError is returned") {
            val result = ipfs.fetchBinaryFileFromDirectory(IpfsHash("testHash"), "example.js")
            assertThat(result).isLeftContaining(IpfsHttpError(exception))
        }
    }

    @Test
    fun `fetchBinaryFileFromDirectory() must return IpfsEmptyResponseError when null response body is returned`() {
        suppose("HTTP client will return a null response") {
            given(blockingClient.retrieve(any<HttpRequest<ByteArray>>(), eq(ByteArray::class.java)))
                .willReturn(null)
            given(properties.localClientPort)
                .willReturn(5001)
        }

        verify("IpfsEmptyResponseError is returned") {
            val hash = IpfsHash("testHash")
            val fileName = "example.js"
            val result = ipfs.fetchBinaryFileFromDirectory(hash, fileName)
            assertThat(result).isLeftContaining(IpfsEmptyResponseError(hash, fileName))
        }
    }

    @Test
    fun `fetchBinaryFileFromDirectory() must correctly substitute {ipfsHash}, use provided port and return a file`() {
        val testPort = 1234
        val hash = IpfsHash("testHash")
        val fileName = "example.js"
        val expectedFileUrl = "http://localhost:$testPort/api/v0/cat?arg=${hash.value}/$fileName"
        val request = HttpRequest.POST(expectedFileUrl, "")
        val response = "example file data".toByteArray()

        suppose("HTTP client will return a file") {
            val httpRequestMatcher: (HttpRequest<ByteArray>) -> Boolean = { arg ->
                arg.uri == request.uri && arg.method == HttpMethod.POST && arg.body == Optional.of("")
            }
            given(properties.localClientPort)
                .willReturn(testPort)
            given(blockingClient.retrieve(argThat(httpRequestMatcher), eq(ByteArray::class.java)))
                .willReturn(response)
        }

        verify("correct file data is returned") {
            val result = ipfs.fetchBinaryFileFromDirectory(hash, fileName)
            assertThat(result).isRightContaining(IpfsBinaryFile(response))
        }
    }

    @Test
    fun `uploadFilesToDirectory() must return IpfsHttpError when HTTP request fails with an exception`() {
        val exception = RuntimeException("http exception")

        suppose("HTTP client will throw an exception") {
            given(reactiveClient.retrieve(any<HttpRequest<String>>(), eq(ByteArray::class.java)))
                .willReturn(Flowable.error(exception))
            given(properties.localClientPort)
                .willReturn(5001)
        }

        verify("IpfsHttpError is returned") {
            val result = ipfs.uploadFilesToDirectory(Flux.just(NamedIpfsFile(ByteArray(0), "fileName"))).block()
            assertThat(result)
                .isNotNull()
                .isLeftContaining(IpfsHttpError(exception))
        }
    }

    @Test
    fun `uploadFilesToDirectory() must return MissingUploadedIpfsDirectoryHash for unknown directory IPFS hash`() {
        val fileName = "fileName"

        suppose("HTTP client will not return IPFS directory hash") {
            given(reactiveClient.retrieve(any<HttpRequest<String>>(), eq(ByteArray::class.java)))
                .willReturn(Flowable.just("{\"Name\":\"$fileName\",\"Hash\":\"fileHash\"}".toByteArray()))
            given(properties.localClientPort)
                .willReturn(5001)
        }

        verify("MissingUploadedIpfsDirectoryHash is returned") {
            val result = ipfs.uploadFilesToDirectory(Flux.just(NamedIpfsFile(ByteArray(0), fileName))).block()
            assertThat(result)
                .isNotNull()
                .isLeftContaining(MissingUploadedIpfsDirectoryHash)
        }
    }

    @Test
    fun `uploadFilesToDirectory() must correctly upload files to IPFS directory`() {
        val fileName = "testFile"
        val fileHash = "fileHash"
        val directoryHash = "directoryHash"

        suppose("HTTP client will return IPFS hashes for uploaded file and wrapping directory") {
            val json = "{\"Name\":\"$fileName\",\"Hash\":\"$fileHash\"}\n{\"Name\":\"\",\"Hash\":\"$directoryHash\"}"

            given(reactiveClient.retrieve(any<HttpRequest<String>>(), eq(ByteArray::class.java)))
                .willReturn(Flowable.just(json.toByteArray()))
            given(properties.localClientPort)
                .willReturn(5001)
        }

        verify("file upload response is returned") {
            val result = ipfs.uploadFilesToDirectory(Flux.just(NamedIpfsFile(ByteArray(0), fileName))).block()
            assertThat(result)
                .isNotNull()
                .isRightContaining(
                    IpfsDirectoryUploadResponse(
                        files = listOf(
                            IpfsFileUploadResponse(fileName = fileName, ipfsHash = IpfsHash(fileHash))
                        ),
                        directoryIpfsHash = IpfsHash(directoryHash)
                    )
                )
        }
    }
}
