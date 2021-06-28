package com.ampnet.auditornode.service

import assertk.assertThat
import assertk.assertions.isInstanceOf
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.isLeftContaining
import com.ampnet.auditornode.isLeftSatisfying
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.model.contract.AssetId
import com.ampnet.auditornode.model.error.RpcError.ContractReadError
import com.ampnet.auditornode.model.error.RpcError.RpcConnectionError
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.service.impl.GethAssetHolderContractService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kethereum.model.Address
import org.kethereum.rpc.EthereumRPC
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import java.math.BigInteger

class GethAssetHolderContractServiceUnitTest : TestBase() {

    private val contractAddress = Address("0xTestContractAddress")
    private val rpcProperties = mock<RpcProperties> {
        on { url } doReturn "http://localhost:8080/test-url"
    }
    private val rpc = mock<EthereumRPC>()
    private val service = GethAssetHolderContractService(rpc, rpcProperties)

    private val testHash = IpfsHash("testHash")
    private val encodedTestHash =
        "0x0000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000" +
            "00000000000000000000087465737448617368000000000000000000000000000000000000000000000000"

    private val testAssetId = AssetId(BigInteger.valueOf(123L))
    private val encodedTestAssetId = "0x000000000000000000000000000000000000000000000000000000000000007b"

    @BeforeEach
    fun beforeEach() {
        reset(rpc)
    }

    @Test
    fun `must return RpcConnectionError when fetching asset info IPFS hash fails`() {
        val exception = RuntimeException("rpc exception")

        suppose("RPC client will throw an exception when fetching asset info IPFS hash") {
            given(rpc.call(any(), any()))
                .willThrow(exception)
        }

        verify("RpcConnectionError is returned") {
            val result = service.getAssetInfoIpfsHash(contractAddress)
            assertThat(result).isLeftContaining(RpcConnectionError(rpcProperties.url, exception))
        }
    }

    @Test
    fun `must return ContractReadError when returned asset info IPFS hash is null`() {
        suppose("RPC client will return null value when fetching asset info IPFS hash") {
            given(rpc.call(any(), any()))
                .willReturn(null)
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
        suppose("RPC client will return some asset info IPFS hash") {
            given(rpc.call(any(), any()))
                .willAnswer { encodedTestHash } // for value classes we must return value with willAnswer
        }

        verify("correct asset info IPFS hash is returned") {
            val result = service.getAssetInfoIpfsHash(contractAddress)
            assertThat(result).isRightContaining(testHash)
        }
    }

    @Test
    fun `must return RpcConnectionError when fetching asset ID fails`() {
        val exception = RuntimeException("rpc exception")

        suppose("RPC client will throw an exception when fetching asset ID") {
            given(rpc.call(any(), any()))
                .willThrow(exception)
        }

        verify("RpcConnectionError is returned") {
            val result = service.getAssetId(contractAddress)
            assertThat(result).isLeftContaining(RpcConnectionError(rpcProperties.url, exception))
        }
    }

    @Test
    fun `must return ContractReadError when returned asset ID is null`() {
        suppose("RPC client will return null value when fetching asset ID") {
            given(rpc.call(any(), any()))
                .willReturn(null)
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
        suppose("RPC client will return some asset ID") {
            given(rpc.call(any(), any()))
                .willAnswer { encodedTestAssetId } // for value classes we must return value with willAnswer
        }

        verify("correct asset category ID is returned") {
            val result = service.getAssetId(contractAddress)
            assertThat(result).isRightContaining(testAssetId)
        }
    }
}
