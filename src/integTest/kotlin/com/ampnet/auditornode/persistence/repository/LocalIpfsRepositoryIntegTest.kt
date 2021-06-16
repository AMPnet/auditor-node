package com.ampnet.auditornode.persistence.repository

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.ampnet.auditornode.IntegTestBase
import com.ampnet.auditornode.isLeftSatisfying
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.isRightSatisfying
import com.ampnet.auditornode.model.error.IpfsError.IpfsHttpError
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.persistence.model.IpfsTextFile
import com.ampnet.auditornode.persistence.repository.impl.LocalIpfsRepository
import com.ampnet.auditornode.testcontainers.IpfsTestContainer
import com.ampnet.auditornode.testcontainers.IpfsTestContainer.uploadFileToIpfs
import com.ampnet.auditornode.testcontainers.IpfsTestContainer.uploadFileToIpfsDirectory
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import javax.inject.Inject

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LocalIpfsRepositoryIntegTest : IntegTestBase(), TestPropertyProvider {

    companion object {
        private val NON_EXISTENT_IPFS_HASH = IpfsHash("QmXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    }

    @Inject
    private lateinit var repository: IpfsRepository

    override fun getProperties(): MutableMap<String, String> =
        mutableMapOf(
            "auditor.use-local-ipfs" to "true",
            "ipfs.local-client-port" to IpfsTestContainer.apiPort().toString(),
            "micronaut.http.client.read-timeout" to "2s"
        )

    @Test
    fun `must correctly fetch text file from IPFS`() {
        val fileContent = "test content"
        var fileHash = IpfsHash("")

        suppose("some file is stored in IPFS") {
            fileHash = client.uploadFileToIpfs(fileContent)
        }

        verify("LocalIpfsRepository implementation is used") {
            assertThat(repository.javaClass)
                .isEqualTo(LocalIpfsRepository::class.java)
        }

        verify("file is correctly fetched from IPFS") {
            val result = repository.fetchTextFile(fileHash)

            assertThat(result)
                .isRightContaining(IpfsTextFile(fileContent))
        }
    }

    @Test
    fun `must return error for non-existent IPFS text file`() {
        verify("LocalIpfsRepository implementation is used") {
            assertThat(repository.javaClass)
                .isEqualTo(LocalIpfsRepository::class.java)
        }

        verify("error is returned for non-existent IPFS file") {
            val result = repository.fetchTextFile(NON_EXISTENT_IPFS_HASH)

            assertThat(result)
                .isLeftSatisfying {
                    assertThat(it)
                        .isInstanceOf(IpfsHttpError::class)
                }
        }
    }

    @Test
    fun `must correctly fetch binary file from IPFS`() {
        val fileContent = "test content"
        var fileHash = IpfsHash("")

        suppose("some file is stored in IPFS") {
            fileHash = client.uploadFileToIpfs(fileContent)
        }

        verify("LocalIpfsRepository implementation is used") {
            assertThat(repository.javaClass)
                .isEqualTo(LocalIpfsRepository::class.java)
        }

        verify("file is correctly fetched from IPFS") {
            val result = repository.fetchBinaryFile(fileHash)

            // we must isRightSatisfying + assertThat because ByteArray does not have sensible equals method
            assertThat(result)
                .isRightSatisfying {
                    assertThat(it.content).isEqualTo(fileContent.toByteArray())
                }
        }
    }

    @Test
    fun `must return error for non-existent IPFS binary file`() {
        verify("LocalIpfsRepository implementation is used") {
            assertThat(repository.javaClass)
                .isEqualTo(LocalIpfsRepository::class.java)
        }

        verify("error is returned for non-existent IPFS file") {
            val result = repository.fetchBinaryFile(NON_EXISTENT_IPFS_HASH)

            assertThat(result)
                .isLeftSatisfying {
                    assertThat(it)
                        .isInstanceOf(IpfsHttpError::class)
                }
        }
    }

    @Test
    fun `must correctly fetch text file from IPFS directory`() {
        val fileContent = "test content"
        val fileName = "test-file"
        var directoryHash = IpfsHash("")

        suppose("some file is stored in IPFS directory") {
            directoryHash = client.uploadFileToIpfsDirectory(fileName = fileName, fileContent = fileContent)
        }

        verify("LocalIpfsRepository implementation is used") {
            assertThat(repository.javaClass)
                .isEqualTo(LocalIpfsRepository::class.java)
        }

        verify("file is correctly fetched from IPFS") {
            val result = repository.fetchTextFileFromDirectory(directoryHash = directoryHash, fileName = fileName)

            assertThat(result)
                .isRightContaining(IpfsTextFile(fileContent))
        }
    }

    @Test
    fun `must return error for non-existent IPFS text file in a directory`() {
        verify("LocalIpfsRepository implementation is used") {
            assertThat(repository.javaClass)
                .isEqualTo(LocalIpfsRepository::class.java)
        }

        verify("error is returned for non-existent IPFS file") {
            val fileName = "test"
            val result = repository.fetchTextFileFromDirectory(
                directoryHash = NON_EXISTENT_IPFS_HASH,
                fileName = fileName
            )

            assertThat(result)
                .isLeftSatisfying {
                    assertThat(it)
                        .isInstanceOf(IpfsHttpError::class)
                }
        }
    }

    @Test
    fun `must correctly fetch binary file from IPFS directory`() {
        val fileContent = "test content"
        val fileName = "test-file"
        var directoryHash = IpfsHash("")

        suppose("some file is stored in IPFS directory") {
            directoryHash = client.uploadFileToIpfsDirectory(fileName = fileName, fileContent = fileContent)
        }

        verify("LocalIpfsRepository implementation is used") {
            assertThat(repository.javaClass)
                .isEqualTo(LocalIpfsRepository::class.java)
        }

        verify("file is correctly fetched from IPFS") {
            val result = repository.fetchBinaryFileFromDirectory(directoryHash = directoryHash, fileName = fileName)

            // we must isRightSatisfying + assertThat because ByteArray does not have sensible equals method
            assertThat(result)
                .isRightSatisfying {
                    assertThat(it.content).isEqualTo(fileContent.toByteArray())
                }
        }
    }

    @Test
    fun `must return error for non-existent IPFS binary file in a directory`() {
        verify("LocalIpfsRepository implementation is used") {
            assertThat(repository.javaClass)
                .isEqualTo(LocalIpfsRepository::class.java)
        }

        verify("error is returned for non-existent IPFS file") {
            val fileName = "test"
            val result = repository.fetchBinaryFileFromDirectory(
                directoryHash = NON_EXISTENT_IPFS_HASH,
                fileName = fileName
            )

            assertThat(result)
                .isLeftSatisfying {
                    assertThat(it)
                        .isInstanceOf(IpfsHttpError::class)
                }
        }
    }
}
