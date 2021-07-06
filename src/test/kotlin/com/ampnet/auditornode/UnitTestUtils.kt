package com.ampnet.auditornode

import com.ampnet.auditornode.model.response.IpfsDirectoryUploadResponse
import com.ampnet.auditornode.model.response.IpfsFileUploadResponse
import com.ampnet.auditornode.model.websocket.WebSocketMessage
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.SingletonSupport
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.fail
import org.mockito.BDDMockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthBlock
import org.web3j.protocol.core.methods.response.EthCall
import org.web3j.protocol.core.methods.response.EthSyncing
import org.web3j.protocol.core.methods.response.NetVersion
import org.web3j.tx.ChainIdLong
import java.math.BigInteger
import java.util.UUID

object UnitTestUtils {

    @Language("RegExp")
    const val UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"

    val objectMapper = ObjectMapper().apply {
        registerModule(KotlinModule(singletonSupport = SingletonSupport.CANONICALIZE))
    }

    fun WebSocketMessage.toJson(): String = objectMapper.writeValueAsString(this)

    fun String.parseScriptId(): UUID? {
        val responseRegex = """^\{"id":"($UUID_REGEX)"}$""".toRegex()
        val matchResult = responseRegex.find(this)
            ?: fail("Response does not match regular expression: $responseRegex")

        return matchResult.groups[1]?.value?.let(UUID::fromString)
    }

    fun String.parseIpfsDirectoryUploadResponse(): IpfsDirectoryUploadResponse {
        val rootNode = objectMapper.readTree(this)
        val files = rootNode["files"].elements().asSequence().map {
            IpfsFileUploadResponse(
                fileName = it["fileName"].asText(),
                ipfsHash = IpfsHash(it["ipfsHash"].asText())
            )
        }.toList()

        return IpfsDirectoryUploadResponse(
            files = files,
            directoryIpfsHash = IpfsHash(rootNode["directoryIpfsHash"].asText())
        )
    }

    fun web3jMockResponse(result: String): Request<*, EthCall> {
        val mockEthCall = mock<EthCall> {
            on { isReverted } doReturn false
            on { value } doReturn result
        }
        return mock {
            on { send() } doReturn mockEthCall
        }
    }

    fun Web3j.mocks(): BDDMockito.BDDMyOngoingStubbing<Request<*, EthCall>> {
        val mockSyncing = mock<EthSyncing> {
            on { isSyncing } doReturn false
        }
        val mockSyncingRequest = mock<Request<*, EthSyncing>> {
            on { send() } doReturn mockSyncing
        }
        given(this.ethSyncing())
            .willReturn(mockSyncingRequest)

        val mockBlock = mock<EthBlock.Block> {
            on { timestamp } doReturn BigInteger.valueOf(Long.MAX_VALUE / 1000L)
        }
        val mockBlockNumber = mock<EthBlock> {
            on { block } doReturn mockBlock
        }
        val mockBlockNumberRequest = mock<Request<*, EthBlock>> {
            on { send() } doReturn mockBlockNumber
        }
        given(this.ethGetBlockByNumber(any(), any()))
            .willReturn(mockBlockNumberRequest)

        val mockNetVersion = mock<NetVersion> {
            on { netVersion } doReturn ChainIdLong.MAINNET.toString()
        }
        val mockNetVersionRequest = mock<Request<*, NetVersion>> {
            on { send() } doReturn mockNetVersion
        }
        given(this.netVersion())
            .willReturn(mockNetVersionRequest)

        val response = web3jMockResponse("0x0000000000000000000000000000000000000000000000000000000000000000")

        return given(this.ethCall(any(), any()))
            .willReturn(response)
            .willReturn(response)
    }
}
