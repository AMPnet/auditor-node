package com.ampnet.auditornode.service

import assertk.assertThat
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.UnitTestUtils.mocks
import com.ampnet.auditornode.UnitTestUtils.web3jMockResponse
import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.model.contract.AssetAuditResult
import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.contract.ContractAddress
import com.ampnet.auditornode.model.contract.LatestAuditTimestamp
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.service.impl.Web3jAssetHolderContractService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.web3j.protocol.Web3j
import java.math.BigInteger

class Web3jAssetHolderContractServiceUnitTest : TestBase() {

    private val contractAddress = ContractAddress("0xTestContractAddress")
    private val rpcProperties = mock<RpcProperties> {
        on { url } doReturn "http://localhost:8080/test-url"
    }
    private val web3j = mock<Web3j>()
    private val service = Web3jAssetHolderContractService(web3j, rpcProperties)

    private val testHash = IpfsHash("testHash")
    private val encodedTestHash =
        "0x0000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000" +
            "00000000000000000000087465737448617368000000000000000000000000000000000000000000000000"

    private val testAssetId = AssetId(BigInteger.valueOf(123L))
    private val encodedTestAssetId = "0x000000000000000000000000000000000000000000000000000000000000007b"

    private val testAssetTypeId = AssetId(BigInteger.valueOf(456L))
    private val encodedTestAssetTypeId = "0x00000000000000000000000000000000000000000000000000000000000001c8"

    private val assetAuditResult = AssetAuditResult(
        verified = true,
        auditInfo = IpfsHash("test"),
        timestamp = LatestAuditTimestamp(BigInteger.valueOf(789L))
    )
    private val encodedOffset = "0000000000000000000000000000000000000000000000000000000000000020"
    private val encodedTrueBoolean = "0000000000000000000000000000000000000000000000000000000000000001"
    private val encodedAuditInfoOffset = "0000000000000000000000000000000000000000000000000000000000000060"
    private val encodedAuditInfo = "000000000000000000000000000000000000000000000000000000000000000474657374000000000" +
        "00000000000000000000000000000000000000000000000"
    private val encodedTimestamp = "0000000000000000000000000000000000000000000000000000000000000315"

    @BeforeEach
    fun beforeEach() {
        reset(web3j)
    }

    @Test
    fun `must correctly return asset ID`() {
        suppose("Web3j client will return some asset ID") {
            val response = web3jMockResponse(encodedTestAssetId)
            web3j.mocks()
                .willReturn(response)
        }

        verify("correct asset category ID is returned") {
            val result = service.getAssetId(contractAddress)
            assertThat(result)
                .isRightContaining(testAssetId)
        }
    }

    @Test
    fun `must correctly return asset type ID`() {
        suppose("Web3j client will return some asset type ID") {
            val response = web3jMockResponse(encodedTestAssetTypeId)
            web3j.mocks()
                .willReturn(response)
        }

        verify("correct asset category ID is returned") {
            val result = service.getAssetId(contractAddress)
            assertThat(result)
                .isRightContaining(testAssetTypeId)
        }
    }

    @Test
    fun `must correctly return asset info IPFS hash`() {
        suppose("Web3j client will return some asset info IPFS hash") {
            val response = web3jMockResponse(encodedTestHash)
            web3j.mocks()
                .willReturn(response)
        }

        verify("correct asset info IPFS hash is returned") {
            val result = service.getAssetInfoIpfsHash(contractAddress)
            assertThat(result)
                .isRightContaining(testHash)
        }
    }

    @Test
    fun `must correctly return latest asset audit`() {
        suppose("Web3j client will return latest asset audit") {
            val response = web3jMockResponse(
                "0x$encodedOffset$encodedTrueBoolean$encodedAuditInfoOffset$encodedTimestamp$encodedAuditInfo"
            )

            web3j.mocks()
                .willReturn(response)
        }

        verify("correct latest asset audit returned") {
            val result = service.getLatestAudit(contractAddress)
            println(result)
            assertThat(result)
                .isRightContaining(assetAuditResult)
        }
    }
}
