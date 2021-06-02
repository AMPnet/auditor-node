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
import com.ampnet.auditornode.service.impl.GethRegistryContractService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kethereum.rpc.EthereumRPC
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import java.math.BigInteger

class GethRegistryContractServiceUnitTest : TestBase() {

    private val auditorProperties = mock<AuditorProperties> {
        on { registryContractAddress } doReturn "0xTestContractAddress"
    }
    private val rpcProperties = mock<RpcProperties> {
        on { url } doReturn "http://localhost:8080/test-url"
    }
    private val rpc = mock<EthereumRPC>()
    private val service = GethRegistryContractService(rpc, auditorProperties, rpcProperties)

    private val testHash = IpfsHash("testHash")
    private val encodedTestHash =
        "0x000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000" +
            "000000000000000000000087465737448617368000000000000000000000000000000000000000000000000"

    @BeforeEach
    fun beforeEach() {
        reset(rpc)
    }

    @Test
    fun `must return RpcConnectionError when fetching auditing procedure directory IPFS hash fails`() {
        val exception = RuntimeException("rpc exception")

        suppose("RPC client will throw an exception when fetching auditing procedure directory IPFS hash") {
            given(rpc.call(any(), any()))
                .willThrow(exception)
        }

        verify("RpcConnectionError is returned") {
            val result = service.getAuditingProcedureDirectoryIpfsHash(AssetCategoryId(BigInteger.ZERO))
            assertThat(result).isLeftContaining(RpcConnectionError(rpcProperties.url, exception))
        }
    }

    @Test
    fun `must return ContractReadError when returned auditing procedure directory IPFS hash is null`() {
        suppose("RPC client will return null value when fetching auditing procedure directory IPFS hash") {
            given(rpc.call(any(), any()))
                .willReturn(null)
        }

        verify("ContractReadError is returned") {
            val result = service.getAuditingProcedureDirectoryIpfsHash(AssetCategoryId(BigInteger.ZERO))
            assertThat(result).isLeftSatisfying {
                assertThat(it).isInstanceOf(ContractReadError::class)
            }
        }
    }

    @Test
    fun `must correctly return auditing procedure directory IPFS hash`() {
        suppose("RPC client will return some auditing procedure directory IPFS hash") {
            given(rpc.call(any(), any()))
                .willAnswer { encodedTestHash } // for inline classes we must return value with willAnswer
        }

        verify("correct auditing procedure directory IPFS hash is returned") {
            val result = service.getAuditingProcedureDirectoryIpfsHash(AssetCategoryId(BigInteger.ZERO))
            assertThat(result).isRightContaining(testHash)
        }
    }
}
