package com.ampnet.auditornode.controller

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import com.ampnet.auditornode.ApiTestWithPropertiesBase
import com.ampnet.auditornode.UnitTestUtils.parseIpfsDirectoryUploadResponse
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.client.multipart.MultipartBody
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MicronautTest(propertySources = ["ipfs-test-properties.yaml"])
class IpfsControllerApiTest : ApiTestWithPropertiesBase("ipfs-test-properties") {

    private val wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(8090))

    @BeforeEach
    fun beforeEach() {
        wireMockServer.start()
    }

    @AfterEach
    fun afterEach() {
        wireMockServer.stop()
    }

    @Test
    fun `must return ok response when single file is found`() {
        val hash = "test"
        val fileContent = "example".toByteArray()

        suppose("IPFS file exists") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/api/v0/cat"))
                    .withQueryParam("arg", equalTo(hash))
                    .willReturn(
                        aResponse()
                            .withBody(fileContent)
                            .withHeader("Content-Type", MediaType.TEXT_PLAIN)
                            .withStatus(200)
                    )
            )
        }

        verify("ok response with file content is returned") {
            val response = client.toBlocking().retrieve(
                HttpRequest.GET<ByteArray>("${serverPath()}/ipfs/$hash"),
                ByteArray::class.java
            )

            assertThat(response)
                .isEqualTo(fileContent)
        }
    }

    @Test
    fun `must return 404 when single file cannot be found`() {
        val hash = "test"

        suppose("IPFS file does not exist") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/api/v0/cat"))
                    .withQueryParam("arg", equalTo(hash))
                    .willReturn(
                        aResponse()
                            .withStatus(404)
                    )
            )
        }

        verify("not found response is returned") {
            assertThat {
                client.toBlocking().retrieve(
                    HttpRequest.GET<ByteArray>("${serverPath()}/ipfs/$hash"),
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
        val directoryHash = "directoryHash"
        val fileName = "test.txt"
        val fileContent = "example".toByteArray()

        suppose("IPFS file exists in a directory") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/api/v0/cat"))
                    .withQueryParam("arg", equalTo("$directoryHash/$fileName"))
                    .willReturn(
                        aResponse()
                            .withBody(fileContent)
                            .withHeader("Content-Type", MediaType.TEXT_PLAIN)
                            .withStatus(200)
                    )
            )
        }

        verify("ok response with file content is returned") {
            val response = client.toBlocking().retrieve(
                HttpRequest.GET<ByteArray>("${serverPath()}/ipfs/$directoryHash/$fileName"),
                ByteArray::class.java
            )

            assertThat(response)
                .isEqualTo(fileContent)
        }
    }

    @Test
    fun `must return 404 response when file from directory cannot be found`() {
        val directoryHash = "directoryHash"
        val fileName = "test.txt"

        suppose("IPFS file does not exist in a directory") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/api/v0/cat"))
                    .withQueryParam("arg", equalTo("$directoryHash/$fileName"))
                    .willReturn(
                        aResponse()
                            .withStatus(404)
                    )
            )
        }

        verify("not found response is returned") {
            assertThat {
                client.toBlocking().retrieve(
                    HttpRequest.GET<ByteArray>("${serverPath()}/ipfs/$directoryHash/$fileName"),
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
        val directoryHash = "directoryHash"
        val fileName = "test-file"
        val fileHash = "fileHash"

        suppose("IPFS file upload will succeed") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/api/v0/add"))
                    .withQueryParam("pin", equalTo("true"))
                    .withQueryParam("quieter", equalTo("true"))
                    .withQueryParam("wrap-with-directory", equalTo("true"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withBody(
                                "{\"Name\":\"\",\"Hash\":\"$directoryHash\"}\n" +
                                    "{\"Name\":\"$fileName\",\"Hash\":\"$fileHash\"}"
                            )
                    )
            )
        }

        verify("ok response for file upload is returned") {
            val requestBody = MultipartBody.builder()
                .addPart("files", fileName, "content".toByteArray())
                .build()
            val request = HttpRequest.POST("${serverPath()}/ipfs/upload", requestBody).apply {
                contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
            }

            val response = client.toBlocking().retrieve(request, String::class.java)?.parseIpfsDirectoryUploadResponse()

            assertThat(response?.directoryIpfsHash)
                .isEqualTo(IpfsHash(directoryHash))
            assertThat(response?.files)
                .isNotNull()
                .hasSize(1)
            assertThat(response?.files?.get(0)?.fileName)
                .isEqualTo(fileName)
            assertThat(response?.files?.get(0)?.ipfsHash)
                .isEqualTo(IpfsHash(fileHash))
        }
    }

    @Test
    fun `must return 500 response for file upload when IPFS file upload fails`() {
        suppose("IPFS file upload will fail") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/api/v0/add"))
                    .withQueryParam("pin", equalTo("true"))
                    .withQueryParam("quieter", equalTo("true"))
                    .withQueryParam("wrap-with-directory", equalTo("true"))
                    .willReturn(
                        aResponse()
                            .withStatus(400)
                    )
            )
        }

        verify("internal server error response is returned") {
            val requestBody = MultipartBody.builder()
                .addPart("files", "fileName", "content".toByteArray())
                .build()
            val request = HttpRequest.POST("${serverPath()}/ipfs/upload", requestBody).apply {
                contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
            }

            assertThat {
                client.toBlocking().retrieve(request, String::class.java)
            }
                .isFailure()
                .isInstanceOf(HttpClientResponseException::class)
                .given {
                    assertThat(it.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
    }
}
