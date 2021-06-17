package com.ampnet.auditornode.testcontainers

import com.ampnet.auditornode.UnitTestUtils
import com.ampnet.auditornode.persistence.model.IpfsHash
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.multipart.MultipartBody
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer

object IpfsTestContainer : GenericContainer<IpfsTestContainer>("ipfs/go-ipfs:v0.8.0") {

    private const val IPFS_API_PORT = 5001
    private const val IPFS_GATEWAY_PORT = 8080

    init {
        withClasspathResourceMapping(
            "com/ampnet/auditornode/testcontainers/ipfs-test-entrypoint.sh",
            "/entrypoint.sh",
            BindMode.READ_ONLY
        )
        withClasspathResourceMapping(
            "com/ampnet/auditornode/testcontainers/ipfs-test-config.json",
            "/data/ipfs/config",
            BindMode.READ_ONLY
        )
        withClasspathResourceMapping(
            "com/ampnet/auditornode/testcontainers/ipfs-test-datastore-spec.json",
            "/data/ipfs/raw_datastore_spec",
            BindMode.READ_ONLY
        )
        withClasspathResourceMapping(
            "com/ampnet/auditornode/testcontainers/ipfs-test-version",
            "/data/ipfs/version",
            BindMode.READ_ONLY
        )

        addExposedPort(5001)
        addExposedPort(8080)

        withCreateContainerCmdModifier {
            it.withEntrypoint("/bin/sh", "/entrypoint.sh")
        }

        env = listOf("IPFS_PROFILE=test")

        start()
    }

    fun apiPort(): Int = getMappedPort(IPFS_API_PORT)

    fun gatewayPort(): Int = getMappedPort(IPFS_GATEWAY_PORT)

    fun RxHttpClient.uploadFileToIpfs(fileContent: String): IpfsHash {
        val fileBytes = fileContent.toByteArray()
        val requestBody = MultipartBody.builder()
            .addPart("file", "test-file", fileBytes)
            .build()
        val request = HttpRequest.POST("http://localhost:${apiPort()}/api/v0/add?quieter=true", requestBody).apply {
            contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
        }

        val response = toBlocking().retrieve(request) ?: ""
        val jsonResponse = UnitTestUtils.objectMapper.readTree(response)

        return IpfsHash(jsonResponse["Hash"].asText())
    }

    fun RxHttpClient.uploadFileToIpfsDirectory(fileName: String, fileContent: String): IpfsHash {
        val fileBytes = fileContent.toByteArray()
        val requestBody = MultipartBody.builder()
            .addPart("file", fileName, fileBytes)
            .build()
        val request = HttpRequest.POST(
            "http://localhost:${apiPort()}/api/v0/add?quieter=true&wrap-with-directory=true",
            requestBody
        ).apply {
            contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
        }

        val response = toBlocking().retrieve(request, ByteArray::class.java) ?: ByteArray(0)
        val responseParts = String(response).split("\n")
            .map(String::trim)
            .filter(String::isNotEmpty)
        val directoryJsonResponse = UnitTestUtils.objectMapper.readTree(responseParts.last())

        return IpfsHash(directoryJsonResponse["Hash"].asText())
    }
}
