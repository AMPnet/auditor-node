package com.ampnet.auditornode.service

import assertk.assertThat
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.UnitTestUtils.mocks
import com.ampnet.auditornode.UnitTestUtils.web3jMockResponse
import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.isRightContaining
import com.ampnet.auditornode.model.contract.Balance
import com.ampnet.auditornode.model.contract.ContractAddress
import com.ampnet.auditornode.model.contract.EthereumAddress
import com.ampnet.auditornode.service.impl.Web3jERC20ContractService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.web3j.protocol.Web3j
import java.math.BigInteger

class Web3jERC20ContractServiceUnitTest : TestBase() {

    private val contractAddress = ContractAddress("0xTestContractAddress")
    private val rpcProperties = mock<RpcProperties> {
        on { url } doReturn "http://localhost:8080/test-url"
    }
    private val web3j = mock<Web3j>()
    private val service = Web3jERC20ContractService(web3j, rpcProperties, contractAddress)

    private val testBalance = Balance(BigInteger.valueOf(123L))
    private val encodedTestBalance = "0x000000000000000000000000000000000000000000000000000000000000007b"
    private val testEthereumAddress = EthereumAddress("0x0000000000000000000000000000000000000001")

    @BeforeEach
    fun beforeEach() {
        reset(web3j)
    }

    @Test
    fun `must correctly return balance of ethereum address`() {
        suppose("Web3j client will return some balance of ethereum address") {
            val response = web3jMockResponse(encodedTestBalance)
            web3j.mocks()
                .willReturn(response)
        }

        verify("correct balance of some ethereum address is returned") {
            val result = service.getBalanceOf(testEthereumAddress)
            assertThat(result)
                .isRightContaining(testBalance)
        }
    }
}
