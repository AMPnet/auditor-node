package com.ampnet.auditornode.controller

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.model.error.IpfsError.IpfsHttpError
import com.ampnet.auditornode.persistence.model.IpfsBinaryFile
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import io.micronaut.http.HttpStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset

class IpfsControllerUnitTest : TestBase() {

    private val ipfsRepository = mock<IpfsRepository>()
    private val controller = IpfsController(ipfsRepository)

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
}
