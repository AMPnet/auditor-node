package com.ampnet.auditornode.service

import assertk.assertThat
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.isLeftContaining
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.model.contract.ContractAddress
import com.ampnet.auditornode.model.error.RpcError.ContractReadError
import com.ampnet.auditornode.model.error.RpcError.RpcConnectionError
import mu.KLogger
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.web3j.tx.exceptions.ContractCallException

class AbstractWeb3jContractServiceUnitTest : TestBase() {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val contractAddress = ContractAddress("0xTestContractAddress")
    }

    private val rpcProperties = mock<RpcProperties> {
        on { url } doReturn "http://localhost:8080/test-url"
    }

    private class TestWeb3jContractService(
        logger: KLogger,
        rpcProperties: RpcProperties
    ) : AbstractWeb3jContractService(logger, rpcProperties) {

        class TestContract(override val contractAddress: ContractAddress) : IContract {
            fun fetch(action: () -> String): String = action()
        }

        override val contractName: String = "test"

        private val contract = TestContract(contractAddress)

        fun fetchFromContract(action: () -> String) =
            getValueFromContract("value", contract, { it.fetch(action) }, { it })
    }

    private val service = TestWeb3jContractService(logger, rpcProperties)

    @Test
    fun `must return RpcConnectionError when fetching fails`() {
        val exception = RuntimeException("rpc exception")
        lateinit var action: () -> String

        suppose("exception will be thrown when fetching a value") {
            action = { throw exception }
        }

        verify("RpcConnectionError is returned") {
            val result = service.fetchFromContract(action)
            assertThat(result)
                .isLeftContaining(RpcConnectionError(rpcProperties.url, exception))
        }
    }

    @Test
    fun `must return ContractReadError when NullPointerException is thrown`() {
        val exception = NullPointerException()
        lateinit var action: () -> String

        suppose("NullPointerException will be thrown when fetching a value") {
            action = { throw exception }
        }

        verify("ContractReadError is returned") {
            val result = service.fetchFromContract(action)
            assertThat(result)
                .isLeftContaining(ContractReadError("Could not fetch value from test contract"))
        }
    }

    @Test
    fun `must return ContractReadError when ContractCallException is thrown`() {
        val exception = ContractCallException("contract call error")
        lateinit var action: () -> String

        suppose("ContractCallException will be thrown when fetching a value") {
            action = { throw exception }
        }

        verify("ContractReadError is returned") {
            val result = service.fetchFromContract(action)
            assertThat(result)
                .isLeftContaining(ContractReadError("Could not fetch value from test contract"))
        }
    }

    @Test
    fun `must correctly return value`() {
        val value = "result"
        lateinit var action: () -> String

        suppose("value will be returned") {
            action = { value }
        }

        verify("ContractReadError is returned") {
            val result = service.fetchFromContract(action)
            assertThat(result)
                .isRightContaining(value)
        }
    }
}
