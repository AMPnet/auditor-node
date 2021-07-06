package com.ampnet.auditornode.controller

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.model.error.IpfsError.IpfsHttpError
import com.ampnet.auditornode.model.error.IpfsError.MissingUploadedIpfsDirectoryHash
import com.ampnet.auditornode.model.response.IpfsDirectoryUploadResponse
import com.ampnet.auditornode.persistence.model.IpfsBinaryFile
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import com.ampnet.auditornode.util.UuidProvider
import io.micronaut.http.HttpStatus
import io.micronaut.http.multipart.CompletedFileUpload
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID
import java.util.stream.Stream

class IpfsControllerUnitTest : TestBase() {

    private val ipfsRepository = mock<IpfsRepository>()
    private val uuidProvider = mock<UuidProvider>()
    private val controller = IpfsController(ipfsRepository, uuidProvider)

    @BeforeEach
    fun beforeEach() {
        reset(ipfsRepository)
    }

    @Test
    fun `must return ok response when single file is found`() {
        val hash = IpfsHash("test")
        val fileContent = IpfsBinaryFile("example".toByteArray())

        suppose("IPFS file exists") {
            given(ipfsRepository.fetchBinaryFile(hash))
                .willReturn(fileContent.right())
        }

        verify("ok response with file content is returned") {
            val response = controller.getFile(hash.value)

            assertThat(response.status)
                .isEqualTo(HttpStatus.OK)
            assertThat(response.body())
                .isEqualTo(fileContent.content)
        }
    }

    @Test
    fun `must return 404 when single file cannot be found`() {
        val hash = IpfsHash("test")

        suppose("IPFS file does not exist") {
            given(ipfsRepository.fetchBinaryFile(hash))
                .willReturn(IpfsHttpError(RuntimeException()).left())
        }

        verify("not found response is returned") {
            val response = controller.getFile(hash.value)

            assertThat(response.status)
                .isEqualTo(HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun `must return ok response when file from directory is found`() {
        val directoryHash = IpfsHash("directoryHash")
        val fileName = "test.txt"
        val fileContent = IpfsBinaryFile("example".toByteArray())

        suppose("IPFS file exists in a directory") {
            given(ipfsRepository.fetchBinaryFileFromDirectory(directoryHash, fileName))
                .willReturn(fileContent.right())
        }

        verify("ok response with file content is returned") {
            val response = controller.getFileFromDirectory(directoryHash.value, fileName)

            assertThat(response.status)
                .isEqualTo(HttpStatus.OK)
            assertThat(response.body())
                .isEqualTo(fileContent.content)
        }
    }

    @Test
    fun `must return 404 response when file from directory cannot be found`() {
        val directoryHash = IpfsHash("directoryHash")
        val fileName = "test.txt"

        suppose("IPFS file does not exist in a directory") {
            given(ipfsRepository.fetchBinaryFileFromDirectory(directoryHash, fileName))
                .willReturn(IpfsHttpError(RuntimeException()).left())
        }

        verify("not found response is returned") {
            val response = controller.getFileFromDirectory(directoryHash.value, fileName)

            assertThat(response.status)
                .isEqualTo(HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun `must return ok response when file upload succeeds`() {
        val filesToUpload = Flux.fromStream(
            Stream.of(
                mock<CompletedFileUpload> {
                    on { bytes } doReturn ByteArray(0)
                    on { filename } doReturn "file1"
                },
                mock<CompletedFileUpload> {
                    on { bytes } doReturn ByteArray(0)
                    on { filename } doReturn null
                }
            )
        )
        val uploadResponse = IpfsDirectoryUploadResponse(
            listOf(),
            IpfsHash("directoryHash")
        )

        suppose("file upload will succeed") {
            given(uuidProvider.getUuid())
                .willReturn(UUID.randomUUID())
            given(ipfsRepository.uploadFilesToDirectory(any()))
                .willReturn(Mono.just(uploadResponse.right()))
        }

        verify("ok response with upload result is returned") {
            val response = Mono.from(controller.uploadFilesToDirectory(filesToUpload)).block()

            assertThat(response?.status)
                .isEqualTo(HttpStatus.OK)
            assertThat(response?.body())
                .isEqualTo(uploadResponse)
        }
    }

    @Test
    fun `must return 500 response when file upload fails`() {
        val filesToUpload = Flux.fromStream(
            Stream.of(
                mock<CompletedFileUpload> {
                    on { bytes } doReturn ByteArray(0)
                    on { filename } doReturn "file1"
                },
                mock<CompletedFileUpload> {
                    on { bytes } doReturn ByteArray(0)
                    on { filename } doReturn "file2"
                }
            )
        )

        suppose("file upload will fail") {
            given(ipfsRepository.uploadFilesToDirectory(any()))
                .willReturn(Mono.just(MissingUploadedIpfsDirectoryHash.left()))
        }

        verify("internal server error response is returned") {
            val response = Mono.from(controller.uploadFilesToDirectory(filesToUpload)).block()

            assertThat(response?.status)
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
