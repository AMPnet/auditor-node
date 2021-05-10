package com.ampnet.auditornode.service

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isNull
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.service.impl.GethEthereumRpc
import org.junit.jupiter.api.Test
import org.kethereum.model.Address
import org.kethereum.model.Transaction
import org.komputing.khex.model.HexString
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.response.EthCall
import java.math.BigInteger

class GethEthereumRpcTest : TestBase() {

    private val web3j = mock<Web3j>()
    private val rpc = GethEthereumRpc(web3j)

    private val notImplementedMessage =
        "Requested method is not implemented; either implement it or use Web3j instance if possible"

    @Test
    fun `blockNumber() should not be implemented`() {
        verify("function is not implemented") {
            assertThat { rpc.blockNumber() }
                .isFailure()
                .hasMessage(notImplementedMessage)
        }
    }

    @Test
    fun `must throw an exception when RPC call fails`() {
        val exception = RuntimeException("rpc call failed")

        suppose("RPC call will fail with an exception") {
            given(web3j.ethCall(any(), any()))
                .willThrow(exception)
        }

        verify("exception with expected message is thrown") {
            assertThat { rpc.call(Transaction(), "latest") }
                .isFailure()
                .isEqualTo(exception)
        }
    }

    @Test
    fun `must return null value when RPC call returns null value`() {
        suppose("RPC call will return null response") {
            val mockEthCallResponse = mock<Request<*, EthCall>> {
                on { send() }.doReturn(null)
            }
            given(web3j.ethCall(any(), any()))
                .willReturn(mockEthCallResponse)
        }

        verify("null value is returned") {
            val result = rpc.call(Transaction(), "latest")
            assertThat(result).isNull()
        }
    }

    @Test
    fun `must return correct value from RPC call`() {
        val expectedValue = "testValue"

        suppose("RPC call will return some value") {
            val mockEthCallResponse = mock<Request<*, EthCall>> {
                on { send() }.doReturn(
                    run {
                        val ethCall = EthCall()
                        ethCall.result = expectedValue
                        ethCall
                    }
                )
            }
            given(web3j.ethCall(any(), any()))
                .willReturn(mockEthCallResponse)
        }

        verify("correct value is returned") {
            val result = rpc.call(Transaction(), "latest")
            assertThat(result).isEqualTo(HexString(expectedValue))
        }
    }

    @Test
    fun `chainId() should not be implemented`() {
        verify("function is not implemented") {
            assertThat { rpc.chainId() }
                .isFailure()
                .hasMessage(notImplementedMessage)
        }
    }

    @Test
    fun `clientVersion() should not be implemented`() {
        verify("function is not implemented") {
            assertThat { rpc.clientVersion() }
                .isFailure()
                .hasMessage(notImplementedMessage)
        }
    }

    @Test
    fun `estimateGas(Transaction) should not be implemented`() {
        verify("function is not implemented") {
            assertThat { rpc.estimateGas(Transaction()) }
                .isFailure()
                .hasMessage(notImplementedMessage)
        }
    }

    @Test
    fun `gasPrice() should not be implemented`() {
        verify("function is not implemented") {
            assertThat { rpc.gasPrice() }
                .isFailure()
                .hasMessage(notImplementedMessage)
        }
    }

    @Test
    fun `getBalance(Address, String) should not be implemented`() {
        verify("function is not implemented") {
            assertThat { rpc.getBalance(Address(""), "") }
                .isFailure()
                .hasMessage(notImplementedMessage)
        }
    }

    @Test
    fun `getBlockByNumber(BigInteger) should not be implemented`() {
        verify("function is not implemented") {
            assertThat { rpc.getBlockByNumber(BigInteger.ZERO) }
                .isFailure()
                .hasMessage(notImplementedMessage)
        }
    }

    @Test
    fun `getCode(String, String) should not be implemented`() {
        verify("function is not implemented") {
            assertThat { rpc.getCode("", "") }
                .isFailure()
                .hasMessage(notImplementedMessage)
        }
    }

    @Test
    fun `getStorageAt(String, String, String) should not be implemented`() {
        verify("function is not implemented") {
            assertThat { rpc.getStorageAt("", "", "") }
                .isFailure()
                .hasMessage(notImplementedMessage)
        }
    }

    @Test
    fun `getTransactionByHash(String) should not be implemented`() {
        verify("function is not implemented") {
            assertThat { rpc.getTransactionByHash("") }
                .isFailure()
                .hasMessage(notImplementedMessage)
        }
    }

    @Test
    fun `getTransactionCount(String, String) should not be implemented`() {
        verify("function is not implemented") {
            assertThat { rpc.getTransactionCount("", "") }
                .isFailure()
                .hasMessage(notImplementedMessage)
        }
    }

    @Test
    fun `sendRawTransaction(String) should not be implemented`() {
        verify("function is not implemented") {
            assertThat { rpc.sendRawTransaction("") }
                .isFailure()
                .hasMessage(notImplementedMessage)
        }
    }
}
