package com.ampnet.auditornode.script.api.classes

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.model.error.IpfsError.IpfsEmptyResponseError
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.IpfsTextFile
import com.ampnet.auditornode.persistence.repository.IpfsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset

class DirectoryBasedIpfsUnitTest : TestBase() {

    private val ipfsRepository = mock<IpfsRepository>()
    private val ipfsHash = IpfsHash("testHash")
    private val service = DirectoryBasedIpfs(ipfsHash, ipfsRepository)

    @BeforeEach
    fun beforeEach() {
        reset(ipfsRepository)
    }

    @Test
    fun `must return null when file cannot be found on IPFS`() {
        val fileName = "test.js"

        suppose("IPFS will return error") {
            given(this.ipfsRepository.fetchTextFileFromDirectory(ipfsHash, fileName))
                .willReturn(IpfsEmptyResponseError(ipfsHash, fileName).left())
        }

        verify("null is returned") {
            assertThat(service.getFile(fileName))
                .isNull()
        }
    }

    @Test
    fun `must return file content from IPFS repository`() {
        val fileName = "test.js"
        val ipfsTextFile = IpfsTextFile("test content")

        suppose("IPFS will return file content") {
            given(this.ipfsRepository.fetchTextFileFromDirectory(ipfsHash, fileName))
                .willReturn(ipfsTextFile.right())
        }

        verify("file content is returned") {
            assertThat(service.getFile(fileName))
                .isEqualTo(ipfsTextFile.content)
        }
    }

    @Test
    fun `must return correct link to the file`() {
        val fileName = "test.js"

        verify("correct file link is returned") {
            assertThat(service.linkToFile(fileName))
                .isEqualTo("/ipfs/${ipfsHash.value}/$fileName")
        }
    }
}
