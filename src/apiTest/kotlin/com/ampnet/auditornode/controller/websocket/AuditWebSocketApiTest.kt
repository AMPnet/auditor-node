package com.ampnet.auditornode.controller.websocket

import com.ampnet.auditornode.ApiTestWithPropertiesBase
import com.ampnet.auditornode.jsAssertions
import com.ampnet.auditornode.model.contract.ContractAddress
import com.ampnet.auditornode.model.contract.UnsignedTransaction
import com.ampnet.auditornode.model.websocket.AuditResultResponse
import com.ampnet.auditornode.model.websocket.ConnectedInfoMessage
import com.ampnet.auditornode.model.websocket.ErrorResponse
import com.ampnet.auditornode.model.websocket.ExecutingInfoMessage
import com.ampnet.auditornode.model.websocket.InvalidInputJsonErrorMessage
import com.ampnet.auditornode.model.websocket.IpfsReadErrorMessage
import com.ampnet.auditornode.model.websocket.RpcErrorMessage
import com.ampnet.auditornode.model.websocket.SpecifyIpfsDirectoryHashCommand
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.script.api.model.AbortedAudit
import com.ampnet.auditornode.script.api.model.SuccessfulAudit
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.micronaut.http.MediaType
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.websocket.RxWebSocketClient
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest(propertySources = ["audit-flow-test-properties.yaml"])
class AuditWebSocketApiTest : ApiTestWithPropertiesBase("audit-flow-test-properties") {

    @Inject
    private lateinit var webSocketClient: RxWebSocketClient

    private val wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(8090))

    private val apxCoordinatorContractAddress = "0x0000000000000000000000000000000000000001"

    private val assetIdRpcCall = "0xaf640d0f"
    private val assetInfoRpcCall = "0x370158ea"

    private val procedureHash = IpfsHash("procedureHash")
    private val testHash = IpfsHash("testHash")
    private val encodedTestHash = "0x00000000000000000000000000000000000000000000000000000000000000200000000000000000" +
        "000000000000000000000000000000000000000000000008746573744861736800000000000000000000000000000000000000000000" +
        "0000"

    private val directoryIpfsHash = IpfsHash("test")
    private val encodedDirectoryIpfsHash = "0000000000000000000000000000000000000000000000000000000000000060000000000" +
        "000000000000000000000000000000000000000000000000000000474657374000000000000000000000000000000000000000000000" +
        "00000000000"

    private val encodedTestAssetId = "0x000000000000000000000000000000000000000000000000000000000000007b"

    private val encodedPerformAuditMethodCall = "0a171092"
    private val encodedTrueBoolean = "0000000000000000000000000000000000000000000000000000000000000001"

    private val assetAddress = ContractAddress("0xTestContractAddress")

    @BeforeEach
    fun beforeEach() {
        wireMockServer.start()
    }

    @AfterEach
    fun afterEach() {
        wireMockServer.stop()
    }

    @Test
    fun `must return RPC error when asset ID cannot be fetched from blockchain`() {
        suppose("asset ID is not readable via RPC") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/rpc"))
                    .withRequestBody(containing(assetIdRpcCall))
                    .willReturn(
                        aResponse()
                            .withStatus(404)
                    )
            )
        }

        verify("RPC error is returned") {
            val client = webSocketClient.connect(
                WebSocketTestClient::class.java,
                "${webSocketPath()}/audit/${assetAddress.value}"
            )
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(RpcErrorMessage("Cannot connect to RPC: http://localhost:8090/rpc"))
            client.close()
        }
    }

    @Test
    fun `must return RPC error when asset info IPFS hash cannot be fetched from blockchain`() {
        suppose("asset ID is returned via RPC") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/rpc"))
                    .withRequestBody(containing(assetIdRpcCall))
                    .willReturn(
                        aResponse()
                            .withBody(
                                """
                                    {
                                        "jsonrpc": "2.0",
                                        "id": 0,
                                        "result": "$encodedTestAssetId"
                                    }
                                """.trimIndent()
                            )
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                            .withStatus(200)
                    )
            )
        }

        suppose("asset info IPFS hash is not readable via RPC") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/rpc"))
                    .withRequestBody(containing(assetInfoRpcCall))
                    .willReturn(
                        aResponse()
                            .withStatus(404)
                    )
            )
        }

        verify("RPC error is returned") {
            val client = webSocketClient.connect(
                WebSocketTestClient::class.java,
                "${webSocketPath()}/audit/${assetAddress.value}"
            )
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(RpcErrorMessage("Cannot connect to RPC: http://localhost:8090/rpc"))
            client.close()
        }
    }

    @Test
    fun `must return IPFS error when asset info file cannot be fetched from IPFS`() {
        suppose("asset ID is returned via RPC") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/rpc"))
                    .withRequestBody(containing(assetIdRpcCall))
                    .willReturn(
                        aResponse()
                            .withBody(
                                """
                                    {
                                        "jsonrpc": "2.0",
                                        "id": 0,
                                        "result": "$encodedTestAssetId"
                                    }
                                """.trimIndent()
                            )
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                            .withStatus(200)
                    )
            )
        }

        suppose("asset info IPFS hash is returned via RPC") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/rpc"))
                    .withRequestBody(containing(assetInfoRpcCall))
                    .willReturn(
                        aResponse()
                            .withBody(
                                """
                                    {
                                        "jsonrpc": "2.0",
                                        "id": 0,
                                        "result": "$encodedTestHash"
                                    }
                                """.trimIndent()
                            )
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                            .withStatus(200)
                    )
            )
        }

        suppose("asset info file is not available on IPFS") {
            wireMockServer.stubFor(
                get(urlPathEqualTo("/ipfs/${testHash.value}"))
                    .willReturn(
                        aResponse()
                            .withStatus(404)
                    )
            )
        }

        verify("IPFS error is returned") {
            val client = webSocketClient.connect(
                WebSocketTestClient::class.java,
                "${webSocketPath()}/audit/${assetAddress.value}"
            )
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(IpfsReadErrorMessage("HTTP error while communicating with IPFS"))
            client.close()
        }
    }

    @Test
    fun `must return JSON parse error when asset info is not a valid JSON`() {
        suppose("asset ID is returned via RPC") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/rpc"))
                    .withRequestBody(containing(assetIdRpcCall))
                    .willReturn(
                        aResponse()
                            .withBody(
                                """
                                    {
                                        "jsonrpc": "2.0",
                                        "id": 0,
                                        "result": "$encodedTestAssetId"
                                    }
                                """.trimIndent()
                            )
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                            .withStatus(200)
                    )
            )
        }

        suppose("asset info IPFS hash is returned via RPC") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/rpc"))
                    .withRequestBody(containing(assetInfoRpcCall))
                    .willReturn(
                        aResponse()
                            .withBody(
                                """
                                    {
                                        "jsonrpc": "2.0",
                                        "id": 0,
                                        "result": "$encodedTestHash"
                                    }
                                """.trimIndent()
                            )
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                            .withStatus(200)
                    )
            )
        }

        suppose("asset info file with invalid JSON is returned via IPFS") {
            wireMockServer.stubFor(
                get(urlPathEqualTo("/ipfs/${testHash.value}"))
                    .willReturn(
                        aResponse()
                            .withBody("invalid json body")
                            .withHeader("Content-Type", MediaType.TEXT_PLAIN)
                            .withStatus(200)
                    )
            )
        }

        verify("invalid input JSON error is returned") {
            val client = webSocketClient.connect(
                WebSocketTestClient::class.java,
                "${webSocketPath()}/audit/${assetAddress.value}"
            )
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(InvalidInputJsonErrorMessage("Error parsing JSON value: invalid json body"))
            client.close()
        }
    }

    @Test
    fun `must return IPFS error when auditing script file cannot be fetched from IPFS`() {
        suppose("asset ID is returned via RPC") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/rpc"))
                    .withRequestBody(containing(assetIdRpcCall))
                    .willReturn(
                        aResponse()
                            .withBody(
                                """
                                    {
                                        "jsonrpc": "2.0",
                                        "id": 0,
                                        "result": "$encodedTestAssetId"
                                    }
                                """.trimIndent()
                            )
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                            .withStatus(200)
                    )
            )
        }

        suppose("asset info IPFS hash is returned via RPC") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/rpc"))
                    .withRequestBody(containing(assetInfoRpcCall))
                    .willReturn(
                        aResponse()
                            .withBody(
                                """
                                    {
                                        "jsonrpc": "2.0",
                                        "id": 0,
                                        "result": "$encodedTestHash"
                                    }
                                """.trimIndent()
                            )
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                            .withStatus(200)
                    )
            )
        }

        suppose("asset info file with valid JSON is returned via IPFS") {
            wireMockServer.stubFor(
                get(urlPathEqualTo("/ipfs/${testHash.value}"))
                    .willReturn(
                        aResponse()
                            .withBody("{}")
                            .withHeader("Content-Type", MediaType.TEXT_PLAIN)
                            .withStatus(200)
                    )
            )
        }

        suppose("auditing script file is not available on IPFS") {
            wireMockServer.stubFor(
                get(urlPathEqualTo("/ipfs/${procedureHash.value}/audit.js"))
                    .willReturn(
                        aResponse()
                            .withStatus(404)
                    )
            )
        }

        verify("IPFS error is returned") {
            val client = webSocketClient.connect(
                WebSocketTestClient::class.java,
                "${webSocketPath()}/audit/${assetAddress.value}"
            )
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(IpfsReadErrorMessage("HTTP error while communicating with IPFS"))
            client.close()
        }
    }

    @Test
    fun `must return audit result message for aborted script`() {
        suppose("asset ID is returned via RPC") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/rpc"))
                    .withRequestBody(containing(assetIdRpcCall))
                    .willReturn(
                        aResponse()
                            .withBody(
                                """
                                    {
                                        "jsonrpc": "2.0",
                                        "id": 0,
                                        "result": "$encodedTestAssetId"
                                    }
                                """.trimIndent()
                            )
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                            .withStatus(200)
                    )
            )
        }

        suppose("asset info IPFS hash is returned via RPC") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/rpc"))
                    .withRequestBody(containing(assetInfoRpcCall))
                    .willReturn(
                        aResponse()
                            .withBody(
                                """
                                    {
                                        "jsonrpc": "2.0",
                                        "id": 0,
                                        "result": "$encodedTestHash"
                                    }
                                """.trimIndent()
                            )
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                            .withStatus(200)
                    )
            )
        }

        suppose("asset info file with valid JSON is returned via IPFS") {
            wireMockServer.stubFor(
                get(urlPathEqualTo("/ipfs/${testHash.value}"))
                    .willReturn(
                        aResponse()
                            .withBody("{\"example\":\"value\"}")
                            .withHeader("Content-Type", MediaType.TEXT_PLAIN)
                            .withStatus(200)
                    )
            )
        }

        suppose("auditing script file is returned via IPFS") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    assertEquals("auditData.example", "value", auditData.example);
                    return AuditResult.aborted("test");
                }
            """.trimIndent()

            wireMockServer.stubFor(
                get(urlPathEqualTo("/ipfs/${procedureHash.value}/audit.js"))
                    .willReturn(
                        aResponse()
                            .withBody(scriptSource)
                            .withHeader("Content-Type", MediaType.TEXT_PLAIN)
                            .withStatus(200)
                    )
            )
        }

        verify("script execution is aborted") {
            val client = webSocketClient.connect(
                WebSocketTestClient::class.java,
                "${webSocketPath()}/audit/${assetAddress.value}"
            )
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(ExecutingInfoMessage)
            client.assertNextMessage(AuditResultResponse(AbortedAudit("test"), null))
            client.close()
        }
    }

    @Test
    fun `must return audit result message for successful script`() {
        suppose("asset ID is returned via RPC") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/rpc"))
                    .withRequestBody(containing(assetIdRpcCall))
                    .willReturn(
                        aResponse()
                            .withBody(
                                """
                                    {
                                        "jsonrpc": "2.0",
                                        "id": 0,
                                        "result": "$encodedTestAssetId"
                                    }
                                """.trimIndent()
                            )
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                            .withStatus(200)
                    )
            )
        }

        suppose("asset info IPFS hash is returned via RPC") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/rpc"))
                    .withRequestBody(containing(assetInfoRpcCall))
                    .willReturn(
                        aResponse()
                            .withBody(
                                """
                                    {
                                        "jsonrpc": "2.0",
                                        "id": 0,
                                        "result": "$encodedTestHash"
                                    }
                                """.trimIndent()
                            )
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                            .withStatus(200)
                    )
            )
        }

        suppose("asset info file with valid JSON is returned via IPFS") {
            wireMockServer.stubFor(
                get(urlPathEqualTo("/ipfs/${testHash.value}"))
                    .willReturn(
                        aResponse()
                            .withBody("{\"example\":\"value\"}")
                            .withHeader("Content-Type", MediaType.TEXT_PLAIN)
                            .withStatus(200)
                    )
            )
        }

        suppose("auditing script file is returned via IPFS") {
            @Language("JavaScript") val scriptSource = jsAssertions + """
                function audit(auditData) {
                    assertEquals("auditData.example", "value", auditData.example);
                    return AuditResult.success();
                }
            """.trimIndent()

            wireMockServer.stubFor(
                get(urlPathEqualTo("/ipfs/${procedureHash.value}/audit.js"))
                    .willReturn(
                        aResponse()
                            .withBody(scriptSource)
                            .withHeader("Content-Type", MediaType.TEXT_PLAIN)
                            .withStatus(200)
                    )
            )
        }

        verify("script is executed successfully") {
            val client = webSocketClient.connect(
                WebSocketTestClient::class.java,
                "${webSocketPath()}/audit/${assetAddress.value}"
            )
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(ExecutingInfoMessage)
            client.assertNextMessage(SpecifyIpfsDirectoryHashCommand(SuccessfulAudit))
            client.send(directoryIpfsHash.value)
            client.assertNextMessage(
                AuditResultResponse(
                    SuccessfulAudit,
                    UnsignedTransaction(
                        to = apxCoordinatorContractAddress,
                        data = "0x$encodedPerformAuditMethodCall${encodedTestAssetId.removePrefix("0x")}" +
                            "$encodedTrueBoolean$encodedDirectoryIpfsHash"
                    )
                )
            )
            client.close()
        }
    }

    @Test
    fun `must return error result message for invalid script`() {
        suppose("asset ID is returned via RPC") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/rpc"))
                    .withRequestBody(containing(assetIdRpcCall))
                    .willReturn(
                        aResponse()
                            .withBody(
                                """
                                    {
                                        "jsonrpc": "2.0",
                                        "id": 0,
                                        "result": "$encodedTestAssetId"
                                    }
                                """.trimIndent()
                            )
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                            .withStatus(200)
                    )
            )
        }

        suppose("asset info IPFS hash is returned via RPC") {
            wireMockServer.stubFor(
                post(urlPathEqualTo("/rpc"))
                    .withRequestBody(containing(assetInfoRpcCall))
                    .willReturn(
                        aResponse()
                            .withBody(
                                """
                                    {
                                        "jsonrpc": "2.0",
                                        "id": 0,
                                        "result": "$encodedTestHash"
                                    }
                                """.trimIndent()
                            )
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                            .withStatus(200)
                    )
            )
        }

        suppose("asset info file with valid JSON is returned via IPFS") {
            wireMockServer.stubFor(
                get(urlPathEqualTo("/ipfs/${testHash.value}"))
                    .willReturn(
                        aResponse()
                            .withBody("{}")
                            .withHeader("Content-Type", MediaType.TEXT_PLAIN)
                            .withStatus(200)
                    )
            )
        }

        suppose("auditing script file is returned via IPFS") {
            @Language("JavaScript") val scriptSource = """
                function audit(auditData) {
                    throw "error";
                }
            """.trimIndent()

            wireMockServer.stubFor(
                get(urlPathEqualTo("/ipfs/${procedureHash.value}/audit.js"))
                    .willReturn(
                        aResponse()
                            .withBody(scriptSource)
                            .withHeader("Content-Type", MediaType.TEXT_PLAIN)
                            .withStatus(200)
                    )
            )
        }

        verify("script is executed with failure") {
            val client = webSocketClient.connect(
                WebSocketTestClient::class.java,
                "${webSocketPath()}/audit/${assetAddress.value}"
            )
                .blockingFirst()
            client.assertNextMessage(ConnectedInfoMessage)
            client.assertNextMessage(ExecutingInfoMessage)
            client.assertNextMessage(ErrorResponse("Error while executing provided script: error"))
            client.close()
        }
    }
}
