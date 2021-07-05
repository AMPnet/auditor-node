package com.ampnet.auditornode.service

import arrow.core.Either
import assertk.assertThat
import assertk.assertions.isInstanceOf
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.UnitTestUtils.mocks
import com.ampnet.auditornode.UnitTestUtils.web3jMockResponse
import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.isLeftContaining
import com.ampnet.auditornode.isLeftSatisfying
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.contract.ContractAddress
import com.ampnet.auditornode.model.error.RpcError.ContractReadError
import com.ampnet.auditornode.model.error.RpcError.RpcConnectionError
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

    @BeforeEach
    fun beforeEach() {
        reset(web3j)
    }

    @Test
    fun `must return RpcConnectionError when fetching asset info IPFS hash fails`() {
        val exception = RuntimeException("rpc exception")

        suppose("Web3j client will throw an exception when fetching asset info IPFS hash") {
            web3j.mocks()
                .willThrow(exception)
        }

        verify("RpcConnectionError is returned") {
            val result = service.getAssetInfoIpfsHash(contractAddress)
            assertThat(result).isLeftContaining(RpcConnectionError(rpcProperties.url, exception))
        }
    }

    @Test
    fun `must return ContractReadError when returned asset info IPFS hash is null`() {
        suppose("Web3j client will return null value when fetching asset info IPFS hash") {
            val response = web3jMockResponse("0x")
            web3j.mocks()
                .willReturn(response)
        }

        verify("ContractReadError is returned") {
            val result = service.getAssetInfoIpfsHash(contractAddress)
            assertThat(result).isLeftSatisfying {
                assertThat(it).isInstanceOf(ContractReadError::class)
            }
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
            assertThat(result).isRightContaining(testHash)
        }
    }

    @Test
    fun `must return RpcConnectionError when fetching asset ID fails`() {
        val exception = RuntimeException("rpc exception")

        suppose("Web3j client will throw an exception when fetching asset ID") {
            web3j.mocks()
                .willThrow(exception)
        }

        verify("RpcConnectionError is returned") {
            val result = service.getAssetId(contractAddress)
            assertThat(result).isLeftContaining(RpcConnectionError(rpcProperties.url, exception))
        }
    }

    @Test
    fun `must return ContractReadError when returned asset ID is null`() {
        suppose("Web3j client will return null value when fetching asset ID") {
            val response = web3jMockResponse("0x")
            web3j.mocks()
                .willReturn(response)
        }

        verify("ContractReadError is returned") {
            val result = service.getAssetId(contractAddress)
            assertThat(result).isLeftSatisfying {
                assertThat(it).isInstanceOf(ContractReadError::class)
            }
        }
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
            assertThat(result).isRightContaining(testAssetId)
        }
    }
}
