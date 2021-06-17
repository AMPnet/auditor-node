package com.ampnet.auditornode.controller

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import com.ampnet.auditornode.IntegTestBase
import com.ampnet.auditornode.IntegTestUtils
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.testcontainers.IpfsTestContainer
import com.ampnet.auditornode.testcontainers.IpfsTestContainer.uploadFileToIpfs
import com.ampnet.auditornode.testcontainers.IpfsTestContainer.uploadFileToIpfsDirectory
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IpfsControllerIntegTest : IntegTestBase(), TestPropertyProvider {

    override fun getProperties(): MutableMap<String, String> =
        mutableMapOf(
            "ipfs.gateway-url" to "http://localhost:${IpfsTestContainer.gatewayPort()}/ipfs/{ipfsHash}"
        )

    @Test
    fun `must return ok response when single file is found`() {
        val fileContent = "test content"
        var fileHash = IpfsHash("")

        suppose("some file is stored in IPFS") {
            fileHash = client.uploadFileToIpfs(fileContent)
        }

        verify("ok response with file content is returned") {
            val response = client.toBlocking().retrieve(
                HttpRequest.GET<ByteArray>("/ipfs/${fileHash.value}"),
                ByteArray::class.java
            )

            assertThat(response)
                .isEqualTo(fileContent.toByteArray())
        }
    }

    @Test
    fun `must return 404 when single file cannot be found`() {
        verify("not found response is returned") {
            assertThat {
                client.toBlocking().retrieve(
                    HttpRequest.GET<ByteArray>("/ipfs/${IntegTestUtils.NON_EXISTENT_IPFS_HASH.value}"),
                    ByteArray::class.java
                )
            }
                .isFailure()
                .isInstanceOf(HttpClientResponseException::class)
                .given {
                    assertThat(it.status).isEqualTo(HttpStatus.NOT_FOUND)
                }
        }
    }

    @Test
    fun `must return ok response when file from directory is found`() {
        val fileContent = "test content"
        val fileName = "test-file"
        var directoryHash = IpfsHash("")

        suppose("some file is stored in IPFS directory") {
            directoryHash = client.uploadFileToIpfsDirectory(fileName = fileName, fileContent = fileContent)
        }

        verify("ok response with file content is returned") {
            val response = client.toBlocking().retrieve(
                HttpRequest.GET<ByteArray>("/ipfs/${directoryHash.value}/$fileName"),
                ByteArray::class.java
            )

            assertThat(response)
                .isEqualTo(fileContent.toByteArray())
        }
    }

    @Test
    fun `must return 404 response when file from directory cannot be found`() {
        verify("not found response is returned") {
            assertThat {
                client.toBlocking().retrieve(
                    HttpRequest.GET<ByteArray>("/ipfs/${IntegTestUtils.NON_EXISTENT_IPFS_HASH.value}/file"),
                    ByteArray::class.java
                )
            }
                .isFailure()
                .isInstanceOf(HttpClientResponseException::class)
                .given {
                    assertThat(it.status).isEqualTo(HttpStatus.NOT_FOUND)
                }
        }
    }
}
