package com.ampnet.auditornode.persistence

import assertk.assertThat
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.configuration.properties.IpfsProperties
import com.ampnet.auditornode.isLeftContaining
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.model.error.IpfsError.IpfsEmptyResponseError
import com.ampnet.auditornode.model.error.IpfsError.IpfsHttpError
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.IpfsTextFile
import com.ampnet.auditornode.persistence.repository.impl.LocalIpfsRepository
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.BlockingHttpClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import java.util.Optional

class LocalIpfsRepositoryTest : TestBase() {

    private val properties = mock<IpfsProperties>()
    private val client = mock<BlockingHttpClient>()
    private val ipfs = LocalIpfsRepository(
        properties,
        client
    )

    @BeforeEach
    fun beforeEach() {
        reset(client, properties)
    }

    @Test
    fun `must return IpfsHttpError when HTTP request fails with an exception`() {
        val exception = RuntimeException("http exception")

        suppose("HTTP client will throw an exception") {
            given(client.retrieve(any<HttpRequest<String>>()))
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
    fun `must return IpfsEmptyResponseError when null response body is returned`() {
        suppose("HTTP client will return a null response") {
            given(client.retrieve(any<HttpRequest<String>>()))
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
    fun `must correctly substitute {ipfsHash}, use provided port and return a file`() {
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
            given(client.retrieve(argThat(httpRequestMatcher)))
                .willReturn(response)
        }

        verify("correct file data is returned") {
            val result = ipfs.fetchTextFile(hash)
            assertThat(result).isRightContaining(IpfsTextFile(response))
        }
    }
}
