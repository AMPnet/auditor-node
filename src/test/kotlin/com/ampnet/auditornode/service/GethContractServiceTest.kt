package com.ampnet.auditornode.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.configuration.properties.AuditorProperties
import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.isLeftContaining
import com.ampnet.auditornode.isLeftSatisfying
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.model.EthereumAddress
import com.ampnet.auditornode.model.error.RpcError.ContractReadError
import com.ampnet.auditornode.model.error.RpcError.RpcConnectionError
import com.ampnet.auditornode.persistence.model.IpfsHash
import com.ampnet.auditornode.service.impl.GethContractService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kethereum.rpc.EthereumRPC
import org.komputing.khex.model.HexString
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthBlockNumber
import java.math.BigInteger

class GethContractServiceTest : TestBase() {

    private val auditorProperties = mock<AuditorProperties> {
        on { contractAddress } doReturn "0xTestContractAddress"
    }
    private val rpcProperties = mock<RpcProperties> {
        on { url } doReturn "http://localhost:8080/test-url"
    }
    private val web3j = mock<Web3j>()
    private val rpc = mock<EthereumRPC>()
    private val service = GethContractService(auditorProperties, rpcProperties, web3j, rpc)

    private val testHash = IpfsHash("testHash")
    private val encodedTestHash = HexString(
        "0x000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000" +
            "000000000000000000000087465737448617368000000000000000000000000000000000000000000000000"
    )

    @BeforeEach
    fun beforeEach() {
        reset(web3j, rpc)
    }

    @Test
    fun `must return RpcConnectionError when fetching current block number fails`() {
        val exception = RuntimeException("rpc exception")

        suppose("web3j client will throw an exception when fetching current block number") {
            given(web3j.ethBlockNumber())
                .willThrow(exception)
        }

        verify("RpcConnectionError is returned") {
            val result = service.currentBlockNumber()
            assertThat(result).isLeftContaining(RpcConnectionError(rpcProperties.url, exception))
        }
    }

    @Test
    fun `must correctly fetch current block number`() {
        val blockNumber = BigInteger.valueOf(12345678)

        suppose("web3j client will return the current block number") {
            val mockBlockNumberResponse = mock<Request<*, EthBlockNumber>> {
                on { send() }.doReturn(
                    run {
                        val ethBlockNumber = EthBlockNumber()
                        ethBlockNumber.result = blockNumber.toString()
                        ethBlockNumber
                    }
                )
            }
            given(web3j.ethBlockNumber())
                .willReturn(mockBlockNumberResponse)
        }

        verify("correct block number is returned") {
            val result = service.currentBlockNumber()
            assertThat(result).isRightContaining(blockNumber)
        }
    }

    @Test
    fun `must return RpcConnectionError when fetching IPFS file hash fails`() {
        val exception = RuntimeException("rpc exception")

        suppose("RPC client will throw an exception when fetching IPFS file hash") {
            given(rpc.call(any(), any()))
                .willThrow(exception)
        }

        verify("RpcConnectionError is returned") {
            val result = service.getIpfsFileHash()
            assertThat(result).isLeftContaining(RpcConnectionError(rpcProperties.url, exception))
        }
    }

    @Test
    fun `must return ContractReadError when returned IPFS file hash is null`() {
        suppose("RPC client will return null value when fetching IPFS file hash") {
            given(rpc.call(any(), any()))
                .willReturn(null)
        }

        verify("ContractReadError is returned") {
            val result = service.getIpfsFileHash()
            assertThat(result).isLeftSatisfying {
                assertThat(it).isInstanceOf(ContractReadError::class)
            }
        }
    }

    @Test
    fun `must correctly return IPFS file hash`() {
        suppose("RPC client will return some IPFS file hash") {
            given(rpc.call(any(), any()))
                .willAnswer { encodedTestHash.string } // for inline classes we must return inner value with willAnswer
        }

        verify("correct IPFS file hash is returned") {
            val result = service.getIpfsFileHash()
            assertThat(result).isRightContaining(testHash)
        }
    }

    @Test
    fun `must correctly generate transaction to store IPFS file hash`() {
        val storeFunctionHash = "92dea922"
        val transactionData = "0x$storeFunctionHash${encodedTestHash.string.removePrefix("0x")}"

        verify("correct transaction to store IPFS file hash is generated") {
            val result = service.storeIpfsFileHash(testHash)
            assertThat(result.to).isEqualTo(EthereumAddress(auditorProperties.contractAddress))
            assertThat(result.data).isEqualTo(transactionData)
        }
    }
}
