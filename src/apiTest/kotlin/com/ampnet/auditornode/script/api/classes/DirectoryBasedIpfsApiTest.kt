package com.ampnet.auditornode.script.api.classes

import assertk.assertThat
import assertk.assertions.isNotNull
import com.ampnet.auditornode.ApiTestWithPropertiesBase
import com.ampnet.auditornode.TestUtils.parseScriptId
import com.ampnet.auditornode.controller.websocket.WebSocketTestClient
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.model.websocket.AuditResultResponse
import com.ampnet.auditornode.model.websocket.ConnectedInfoMessage
import com.ampnet.auditornode.model.websocket.ExecutingInfoMessage
import com.ampnet.auditornode.script.api.model.AuditResult
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.websocket.RxWebSocketClient
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import javax.inject.Inject

@MicronautTest(propertySources = ["ipfs-test-properties.yaml"])
class DirectoryBasedIpfsApiTest : ApiTestWithPropertiesBase("ipfs-test-properties") {

    @Inject
    private lateinit var webSocketClient: RxWebSocketClient

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
    fun `must execute script which uses getFile() call`() {
        val ipfsDirectoryHash = "testHash"
        val fileName1 = "file1"
        val content1 = "example file 1"
        val fileName2 = "file2"
        val content2 = "example file 2"

        suppose("some files are available via IPFS") {
            wireMockServer.stubFor(
                get(urlPathEqualTo("/ipfs/$ipfsDirectoryHash/$fileName1"))
                    .willReturn(
                        aResponse()
                            .withBody(content1)
                            .withHeader("Content-Type", MediaType.TEXT_PLAIN)
                            .withStatus(200)
                    )
            )

            wireMockServer.stubFor(
                get(urlPathEqualTo("/ipfs/$ipfsDirectoryHash/$fileName2"))
                    .willReturn(
                        aResponse()
                            .withBody(content2)
                            .withHeader("Content-Type", MediaType.TEXT_PLAIN)
                            .withStatus(200)
                    )
            )
        }

        var storedScriptId: UUID? = null

        suppose("script is stored for interactive execution") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit() {
                    let file1Content = Ipfs.getFile("file1");
                    assertEquals("file1Content", "example file 1", file1Content);

                    let file2Content = Ipfs.getFile("file2");
                    assertEquals("file2Content", "example file 2", file2Content);

                    return AuditResult.of(true);
                }
            """.trimIndent()

            val result = client.toBlocking().retrieve(
                HttpRequest.POST("${serverPath()}/script/store", scriptSource).apply {
                    contentType(MediaType.TEXT_PLAIN_TYPE)
                }
            )

            storedScriptId = result.parseScriptId()
            assertThat(storedScriptId).isNotNull()
        }

        verify("correct files are returned") {
            val client = webSocketClient.connect(
                WebSocketTestClient::class.java,
                "${webSocketPath()}/script/interactive/$storedScriptId?ipfs-directory=$ipfsDirectoryHash"
            )
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(ExecutingInfoMessage)
            client.assertNextMessage(AuditResultResponse(AuditResult(true)))
            client.close()
        }
    }
}
