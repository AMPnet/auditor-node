package com.ampnet.auditornode.controller

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import com.ampnet.auditornode.IntegTestBase
import com.ampnet.auditornode.IntegTestUtils
import com.ampnet.auditornode.UnitTestUtils.parseIpfsDirectoryUploadResponse
import com.ampnet.auditornode.model.response.IpfsDirectoryUploadResponse
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.testcontainers.IpfsTestContainer
import com.ampnet.auditornode.testcontainers.IpfsTestContainer.uploadFileToIpfs
import com.ampnet.auditornode.testcontainers.IpfsTestContainer.uploadFileToIpfsDirectory
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.client.multipart.MultipartBody
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IpfsControllerIntegTest : IntegTestBase(), TestPropertyProvider {

    override fun getProperties(): MutableMap<String, String> =
        mutableMapOf(
            "auditor.use-local-ipfs" to "true",
            "ipfs.local-client-port" to IpfsTestContainer.apiPort().toString(),
            "micronaut.http.client.read-timeout" to "2s",
            "micronaut.http.services.test-client.read-timeout" to "10s"
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

    @Test
    fun `must return ok response when files are successfully stored`() {
        val file1Content = "content1".toByteArray()
        val file2Content = "content2".toByteArray()
        var response: IpfsDirectoryUploadResponse? = null

        verify("ok response for file upload is returned") {
            val requestBody = MultipartBody.builder()
                .addPart("files", "file1", file1Content)
                .addPart("files", "file2", file2Content)
                .build()
            val request = HttpRequest.POST("/ipfs/upload", requestBody).apply {
                contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
            }

            response = client.toBlocking().retrieve(request, String::class.java)?.parseIpfsDirectoryUploadResponse()

            assertThat(response?.directoryIpfsHash)
                .isNotNull()
            assertThat(response?.files)
                .isNotNull()
                .hasSize(2)
            assertThat(response?.files?.get(0)?.fileName)
                .isEqualTo("file1")
            assertThat(response?.files?.get(1)?.fileName)
                .isEqualTo("file2")
        }

        verify("files are correctly uploaded") {
            assertThat(response)
                .isNotNull()

            val nonNullResponse = response!!
            val directoryHash = nonNullResponse.directoryIpfsHash

            val file1Name = nonNullResponse.files[0].fileName
            val file1Response = client.toBlocking().retrieve(
                HttpRequest.GET<ByteArray>("/ipfs/${directoryHash.value}/$file1Name"),
                ByteArray::class.java
            )

            assertThat(file1Response)
                .isEqualTo(file1Content)

            val file2Name = nonNullResponse.files[1].fileName
            val file2Response = client.toBlocking().retrieve(
                HttpRequest.GET<ByteArray>("/ipfs/${directoryHash.value}/$file2Name"),
                ByteArray::class.java
            )

            assertThat(file2Response)
                .isEqualTo(file2Content)
        }
    }
}
