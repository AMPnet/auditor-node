package com.ampnet.auditornode.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.ampnet.auditornode.TestBase
import com.ampnet.auditornode.configuration.properties.RpcProperties
import com.ampnet.auditornode.model.contract.ContractAddress
import com.ampnet.auditornode.service.impl.Web3jAssetHolderContractService
import com.ampnet.auditornode.service.impl.Web3jAssetListHolderContractService
import com.ampnet.auditornode.service.impl.Web3jContractProvider
import com.ampnet.auditornode.service.impl.Web3jERC20ContractService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.web3j.protocol.Web3j

class Web3jContractProviderUnitTest : TestBase() {

    private val web3j = mock<Web3j>()
    private val rpcProperties = mock<RpcProperties>()
    private val testContractAddress = ContractAddress("0xTestContractAddress")
    private val provider = Web3jContractProvider(web3j, rpcProperties)

    @Test
    fun `must correctly provide asset holder contract`() {
        verify("contract provider returns correct asset holder contract") {
            val result = provider.getAssetHolderContract(testContractAddress)
            assertThat(result).isInstanceOf(Web3jAssetHolderContractService::class)
            assertThat(result.contractAddress).isEqualTo(testContractAddress)
        }
    }

    @Test
    fun `must correctly provide asset list holder contract`() {
        verify("contract provider returns correct asset list holder contract") {
            val result = provider.getAssetListHolderContract(testContractAddress)
            assertThat(result).isInstanceOf(Web3jAssetListHolderContractService::class)
            assertThat(result.contractAddress).isEqualTo(testContractAddress)
        }
    }

    @Test
    fun `must correctly provide ERC20 contract`() {
        verify("contract provider returns correct ERC20 contract") {
            val result = provider.getERC20Contract(testContractAddress)
            assertThat(result).isInstanceOf(Web3jERC20ContractService::class)
            assertThat(result.contractAddress).isEqualTo(testContractAddress)
        }
    }
}
