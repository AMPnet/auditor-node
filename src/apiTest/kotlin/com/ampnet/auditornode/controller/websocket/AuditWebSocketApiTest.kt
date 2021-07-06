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
import org.web3j.ens.Contracts
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

    private val nullAddress = "0x0000000000000000000000000000000000000000000000000000000000000000"
    private val extraData = "0x726f6e696e2d6b61697a656e00000000000000000000000000000000000000005d6cde45513868d575627f" +
        "26971bcb2c23b22f084c05b328ec42d2b76f0e30fc02167705b63e521fea57a9996321c98fbb664e4045219e69d9adabcd6afaf03c00"
    private val logsBloom = "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
        "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
        "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
        "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
        "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
    private val ethGetBlockByNumberResponse =
        """
            {
                "jsonrpc": "2.0",
                "id": 1,
                "result": {
                    "baseFeePerGas": "0x7",
                    "difficulty": "0x1",
                    "extraData": "$extraData",
                    "gasLimit": "0x1c9c380",
                    "gasUsed": "0x1c99c70",
                    "hash": "0xbf3d128841b4f13d566dfe61909767b38c473a9f7529588bf9531cde6123f6a8",
                    "logsBloom": "$logsBloom",
                    "miner": "0x0000000000000000000000000000000000000000",
                    "mixHash": "$nullAddress",
                    "nonce": "0x0000000000000000",
                    "number": "0x4d48e4",
                    "parentHash": "0xb99014bb4e5f96e5e568018178fd0e56d559ce6fa03d8233f338a143d0aeb6ed",
                    "receiptsRoot": "0x585cbd12b64a145c8c20097643c9fe0de9ded99ad39fde24b5183a07a2ec1c69",
                    "sha3Uncles": "0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347",
                    "size": "0x2d3",
                    "stateRoot": "0xc87c56d7bc42118f4ea122329a7ad7d0bf7a9d2bdc5391df4e81bb694b8e7015",
                    "timestamp": "0xffffffff",
                    "totalDifficulty": "0x70ffdc",
                    "transactions": [
                        "0xf7bb636848ead6be84236d9819aa1563d2df1211a74ba5e0a90b675ff5e876bd"
                    ],
                    "transactionsRoot": "0x040e7f7b79986589387957ea9a6d807577731c92bb05fc70ddd015409de91e35",
                    "uncles": []
                }
            }
        """.trimIndent()

    @BeforeEach
    fun beforeEach() {
        wireMockServer.start()
    }

    @AfterEach
    fun afterEach() {
        wireMockServer.stop()
    }

    private fun supposeRpcIsReachable() {
        wireMockServer.stubFor(
            post(urlPathEqualTo("/rpc"))
                .withRequestBody(containing("eth_syncing"))
                .willReturn(
                    aResponse()
                        .withBody(
                            """
                                {
                                    "jsonrpc": "2.0",
                                    "id": 0,
                                    "result": false
                                }
                            """.trimIndent()
                        )
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                        .withStatus(200)
                )
        )
        wireMockServer.stubFor(
            post(urlPathEqualTo("/rpc"))
                .withRequestBody(containing("eth_getBlockByNumber"))
                .willReturn(
                    aResponse()
                        .withBody(ethGetBlockByNumberResponse)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                        .withStatus(200)
                )
        )
        wireMockServer.stubFor(
            post(urlPathEqualTo("/rpc"))
                .withRequestBody(containing("net_version"))
                .willReturn(
                    aResponse()
                        .withBody(
                            """
                                {
                                    "jsonrpc": "2.0",
                                    "id": 2,
                                    "result": "1"
                                }
                            """.trimMargin()
                        )
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                        .withStatus(200)
                )
        )
        wireMockServer.stubFor(
            post(urlPathEqualTo("/rpc"))
                .withRequestBody(containing(Contracts.MAINNET))
                .willReturn(
                    aResponse()
                        .withBody(
                            """
                                {
                                    "jsonrpc": "2.0",
                                    "id": 3,
                                    "result": "$nullAddress"
                                }
                            """.trimMargin()
                        )
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                        .withStatus(200)
                )
        )
        wireMockServer.stubFor(
            post(urlPathEqualTo("/rpc"))
                .withRequestBody(containing("0x0000000000000000000000000000000000000000"))
                .willReturn(
                    aResponse()
                        .withBody(
                            """
                                {
                                    "jsonrpc": "2.0",
                                    "id": 4,
                                    "result": "$nullAddress"
                                }
                            """.trimMargin()
                        )
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                        .withStatus(200)
                )
        )
    }

    @Test
    fun `must return RPC error when asset ID cannot be fetched from blockchain`() {
        supposeRpcIsReachable()

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
        supposeRpcIsReachable()

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
                                        "id": 5,
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
        supposeRpcIsReachable()

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
                                        "id": 5,
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
                                        "id": 6,
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
        supposeRpcIsReachable()

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
                                        "id": 5,
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
                                        "id": 6,
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
        supposeRpcIsReachable()

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
                                        "id": 5,
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
                                        "id": 6,
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
        supposeRpcIsReachable()

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
                                        "id": 5,
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
                                        "id": 6,
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
        supposeRpcIsReachable()

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
                                        "id": 5,
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
                                        "id": 6,
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
        supposeRpcIsReachable()

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
                                        "id": 5,
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
                                        "id": 6,
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
