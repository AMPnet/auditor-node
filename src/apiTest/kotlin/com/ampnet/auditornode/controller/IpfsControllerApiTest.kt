package com.ampnet.auditornode.controller

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import com.ampnet.auditornode.ApiTestWithPropertiesBase
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.exceptions.HttpClientResponseException
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
                get(urlPathEqualTo("/ipfs/$hash"))
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
                get(urlPathEqualTo("/ipfs/$hash"))
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
                get(urlPathEqualTo("/ipfs/$directoryHash/$fileName"))
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
        val directoryHash = IpfsHash("directoryHash")
        val fileName = "test.txt"

        suppose("IPFS file does not exist in a directory") {
            wireMockServer.stubFor(
                get(urlPathEqualTo("/ipfs/$directoryHash/$fileName"))
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
}
