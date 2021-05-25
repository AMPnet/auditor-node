package com.ampnet.auditornode.service

import assertk.assertThat
import assertk.assertions.isInstanceOf
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.configuration.properties.AuditorProperties
import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.isLeftContaining
import com.ampnet.auditornode.isLeftSatisfying
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.model.error.RpcError.ContractReadError
import com.ampnet.auditornode.model.error.RpcError.RpcConnectionError
import com.ampnet.auditornode.persistence.model.AssetCategoryId
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.service.impl.GethAssetContractService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kethereum.rpc.EthereumRPC
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import java.math.BigInteger

class GethAssetContractServiceTest : TestBase() {

    private val auditorProperties = mock<AuditorProperties> {
        on { assetContractAddress } doReturn "0xTestContractAddress"
    }
    private val rpcProperties = mock<RpcProperties> {
        on { url } doReturn "http://localhost:8080/test-url"
    }
    private val rpc = mock<EthereumRPC>()
    private val service = GethAssetContractService(auditorProperties, rpcProperties, rpc)

    private val testHash = IpfsHash("testHash")
    private val encodedTestHash =
        "0x000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000" +
            "000000000000000000000087465737448617368000000000000000000000000000000000000000000000000"

    private val testAssetCategoryId = AssetCategoryId(BigInteger.valueOf(123L))
    private val encodedTestAssetCategoryId = "0x000000000000000000000000000000000000000000000000000000000000007b"

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
            val result = service.getAssetInfoIpfsHash()
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
            val result = service.getAssetInfoIpfsHash()
            assertThat(result).isLeftSatisfying {
                assertThat(it).isInstanceOf(ContractReadError::class)
            }
        }
    }

    @Test
    fun `must correctly return asset info IPFS hash`() {
        suppose("RPC client will return some asset info IPFS hash") {
            given(rpc.call(any(), any()))
                .willAnswer { encodedTestHash } // for inline classes we must return value with willAnswer
        }

        verify("correct asset info IPFS hash is returned") {
            val result = service.getAssetInfoIpfsHash()
            assertThat(result).isRightContaining(testHash)
        }
    }

    @Test
    fun `must return RpcConnectionError when fetching asset category ID fails`() {
        val exception = RuntimeException("rpc exception")

        suppose("RPC client will throw an exception when fetching asset category ID") {
            given(rpc.call(any(), any()))
                .willThrow(exception)
        }

        verify("RpcConnectionError is returned") {
            val result = service.getAssetCategoryId()
            assertThat(result).isLeftContaining(RpcConnectionError(rpcProperties.url, exception))
        }
    }

    @Test
    fun `must return ContractReadError when returned asset category ID is null`() {
        suppose("RPC client will return null value when fetching asset category ID") {
            given(rpc.call(any(), any()))
                .willReturn(null)
        }

        verify("ContractReadError is returned") {
            val result = service.getAssetCategoryId()
            assertThat(result).isLeftSatisfying {
                assertThat(it).isInstanceOf(ContractReadError::class)
            }
        }
    }

    @Test
    fun `must correctly return asset category ID`() {
        suppose("RPC client will return some asset category ID") {
            given(rpc.call(any(), any()))
                .willAnswer { encodedTestAssetCategoryId } // for inline classes we must return value with willAnswer
        }

        verify("correct asset category ID is returned") {
            val result = service.getAssetCategoryId()
            assertThat(result).isRightContaining(testAssetCategoryId)
        }
    }
}
